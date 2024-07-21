package com.isel.gomokuApi.services

import com.isel.gomokuApi.domain.GameDomain
import com.isel.gomokuApi.domain.gameLogic.openingRules.OpeningRuleError
import com.isel.gomokuApi.domain.gameLogic.openingRules.OpeningRuleOperation
import com.isel.gomokuApi.domain.gameLogic.openingRules.OpeningRuleResult
import com.isel.gomokuApi.domain.model.game.*
import com.isel.gomokuApi.http.model.BoardConfig
import com.isel.gomokuApi.repository.TransactionManager
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Play
import com.isel.gomokuApi.services.model.*
import com.isel.gomokuApi.utils.*
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

sealed class GameValidationError {
    object InvalidGrid : GameValidationError()
    object InvalidPosition : GameValidationError()
    object InvalidTurn : GameValidationError()
    object InvalidOpeningRule : GameValidationError()
    object LobbyNotFound : GameValidationError()
    object InvalidVariant : GameValidationError()
    object UnauthorizedPlayer : GameValidationError()
    object UserAlreadyInGame : GameValidationError()
    object WaitingForOpponent : GameValidationError()
    object NoSelectedGoPiece : GameValidationError()
    object BoardAlreadySetup : GameValidationError()
    object BoardNotOpen : GameValidationError()

    class InsufficientPieces(val possiblePieces: List<Int>) : GameValidationError()

}
typealias GameOperationResult = Either<GameValidationError, GameStatus>

@Component
class GameServices(
    private val transactionManager: TransactionManager,
    private val gameDomain: GameDomain,
    private val clock: Clock
) {

    fun startGame(grid: Int, desiredRule: String, desiredVariant: String, userId: Int): GameOperationResult {
        //Input validations
        val variant = gameDomain.locateVariant(desiredVariant) ?: return failure(GameValidationError.InvalidVariant)
        if (!gameDomain.gridValidation(grid)) return failure(GameValidationError.InvalidGrid)
        val openingRule =
            gameDomain.openingRuleValidation(desiredRule) ?: return failure(GameValidationError.InvalidOpeningRule)

        return transactionManager.run {
            val gameRepo = it.gameRepository
            //Get all id from Match without endTime -- see if user is in a lobby with that match id
            if (gameRepo.getMatchByUser(userId) != null) {
                return@run failure(GameValidationError.UserAlreadyInGame)
            }

            val lobby = gameRepo.searchLobby(grid, openingRule, variant)

            if (lobby != null) {

                //See if user is already in a lobby
                if (lobby.userAId == userId) return@run failure(GameValidationError.WaitingForOpponent)

                gameRepo.updateLobby(lobby.id, userId)
                val blackId : Int? = gameDomain.assignIdOnCreation(openingRule,lobby.userAId,userId)

                gameRepo.createMatch(lobby.id, blackId, clock.now().epochSeconds)
                return@run success(WaitingOpponentPieces(lobby.id,lobby.grid, emptyList(),lobby.userAId))

            } else {
                val lobbyId = gameRepo.createLobby(userId, grid, openingRule, variant)
                return@run success(AwaitingOpponent(lobbyId,grid))

            }
        }
    }

    fun quitGame(gameId: Int, userId: Int): GameOperationResult {
        return transactionManager.run {
            val gameRepo = it.gameRepository
            val lobby = gameRepo.getLobby(gameId) ?: return@run failure(GameValidationError.LobbyNotFound)
            //Check if player belongs to game
            if (!gameDomain.belongsToMatch(lobby, userId)) {
                return@run failure(GameValidationError.UnauthorizedPlayer)
            }

            //User is alone in a lobby
            if (lobby.userBId == null) {
                gameRepo.closeLobby(gameId)
                return@run success(LobbyClosed(gameId))
            }
            //Check if game as already ended
            val gameState = gameRepo.getOutcome(lobby.id)
            val moves = gameDomain.convertToMoves(gameRepo.getPlays(lobby.id))
            val match = gameRepo.getMatch(lobby.id)!!
            val opponent = if (lobby.userAId == userId) lobby.userBId else lobby.userAId
            if (gameState != null) {
                //Can be null if the users have not yet selected all go Piece when preparating the board
                val activePiece =
                    if (match.blackPieceId != null)gameDomain.determinePiece(match.blackPieceId, userId) else null
                val opponent = if (lobby.userAId == userId) lobby.userBId else lobby.userAId
                return@run success(GameEnded(gameState.winner,lobby.grid,moves,activePiece,opponent))
            }

            //Delegate win
            val startTime: Long =
                gameRepo.getStartTime(lobby.id) ?: return@run failure(GameValidationError.LobbyNotFound)
            val duration = gameDomain.getGameDuration(startTime, clock.now())
            val outcome = if (userId == lobby.userAId) {
                GameOutcome(
                    lobby.id,
                    lobby.userBId,
                    GamePoints.DEFEAT,
                    GamePoints.VICTORY,
                    duration
                )
            } else {
                GameOutcome(
                    lobby.id,
                    lobby.userAId,
                    GamePoints.DEFEAT,
                    GamePoints.VICTORY,
                    duration
                )
            }
            gameRepo.createOutcome(outcome)
            //Can be null if the users have not yet selected all go Piece when preparating the board
            val activePiece =
                if (match.blackPieceId != null)gameDomain.determinePiece(match.blackPieceId, userId) else null
            return@run success(GameEnded(outcome.winner,lobby.grid,moves,activePiece,opponent))
        }
    }

    //TODO:Add controller
    fun matchState(lobbyId: Int, userId: Int): GameOperationResult = transactionManager.run {
        val gameRepo = it.gameRepository
        val lobby = gameRepo.getLobby(lobbyId) ?: return@run failure(GameValidationError.LobbyNotFound)
        if (!gameDomain.belongsToMatch(lobby, userId)) {
            return@run failure(GameValidationError.UnauthorizedPlayer)
        }
        val match = gameRepo.getMatch(lobbyId) ?: return@run success(AwaitingOpponent(lobbyId,lobby.grid))
        val moves = gameDomain.convertToMoves(gameRepo.getPlays(lobbyId))
        val opponent = if (lobby.userAId == userId) {
            lobby.userBId ?: return@run success(AwaitingOpponent(lobbyId,lobby.grid))
        } else lobby.userAId
        if (match.endTime != null) {
            val gameState = gameRepo.getOutcome(lobbyId) ?: throw IllegalStateException(
                "Business integrity was not fulfilled, match as endtime but there is no GameOutcome"
            )
            //Can be null if the users have not yet selected all go Piece when preparating the board
            val activePiece = if (match.blackPieceId != null)gameDomain.determinePiece(match.blackPieceId, userId) else null

            return@run success(GameEnded(gameState.winner,lobby.grid,moves,activePiece,opponent))
        }

        return@run if (match.blackPieceId != null) {
            //The Game is running
            val activePiece = gameDomain.determinePiece(match.blackPieceId, userId)
            val turn = gameDomain.validateTurn(moves, activePiece)
            success(
                GameRunning(lobbyId,lobby.grid, activePiece, turn,moves,opponent)
            )
        } else {
            //Board preparing stage
            val (determineTurn,requestedPieces) = gameDomain.placePieceTurn(lobby,userId,moves.size)
            success(
                if (determineTurn) GameOpened(lobbyId,lobby.grid,requestedPieces,moves,opponent) else WaitingOpponentPieces(lobbyId,lobby.grid,moves,opponent)
            )
        }
    }

    fun makePlay(position: Position, lobbyId: Int, userId: Int): GameOperationResult {
        return transactionManager.run {
            val gameRepo = it.gameRepository
            val lobby = gameRepo.getLobby(lobbyId) ?: return@run failure(GameValidationError.LobbyNotFound)
            if (gameDomain.checkGridConstraints(position, lobby.grid)) {
                //Play out of bounds
                return@run failure(GameValidationError.InvalidPosition)
            }
            //Does user belong to match
            if (!gameDomain.belongsToMatch(lobby, userId))
                return@run failure(GameValidationError.UnauthorizedPlayer)
            //See if game exists Match
            val match = gameRepo.getMatch(lobbyId) ?: return@run failure(GameValidationError.WaitingForOpponent)

            if (match.blackPieceId == null) return@run failure(GameValidationError.BoardNotOpen)
            //Check if game as ended
            val moves = gameDomain.convertToMoves(
                gameRepo.getPlays(lobbyId)
            )
            val activePiece = gameDomain.determinePiece(match.blackPieceId, userId)

            val opponent = if (lobby.userAId == userId) {
                lobby.userBId ?: return@run success(AwaitingOpponent(lobbyId,lobby.grid))
            } else lobby.userAId
            if (match.endTime != null) {
                val gameState = gameRepo.getOutcome(lobbyId) ?: throw IllegalStateException(
                    "Business integrity was not fulfilled, match as endtime but there is no GameOutcome"
                )
                return@run success(GameEnded(gameState.winner,lobby.grid,moves,activePiece,opponent))
            }


            if (!gameDomain.validateTurn(moves, activePiece)) {
                return@run failure(GameValidationError.InvalidTurn)
            }

            if (gameDomain.positionValidation(position, moves) != null) {
                //Play already made
                return@run failure(GameValidationError.InvalidPosition)
            }


            val board = gameDomain.makeBoard(lobby.variant, lobby.grid, moves, activePiece)

            val newMoves = moves + Move(position,activePiece)
            gameRepo.storePlay(Play(match.id, position/*activePiece*/))

            return@run when (board.play(position)) {
                GameState.DRAW -> {
                    val startTime: Long =
                        gameRepo.getStartTime(lobby.id) ?: return@run failure(GameValidationError.LobbyNotFound)
                    val outcome: GameOutcome = gameDomain.createOutcome(lobby, null, startTime, clock.now())
                    gameRepo.createOutcome(outcome)
                    success(GameEnded(null,lobby.grid,newMoves,activePiece,opponent))
                }

                GameState.WINNER -> {
                    val startTime: Long =
                        gameRepo.getStartTime(lobby.id) ?: return@run failure(GameValidationError.LobbyNotFound)
                    val outcome: GameOutcome =
                        gameDomain.createOutcome(lobby, userId, startTime, clock.now())
                    gameRepo.createOutcome(outcome)
                    success(GameEnded(userId,lobby.grid,newMoves,activePiece,opponent))
                }

                GameState.RUN -> success(PlayMade(lobbyId))
            }
        }
    }

    fun fetchActiveGameByUser(userId: Int): GameOperationResult{
        return transactionManager.run {
            val gameRepo = it.gameRepository
            val lobby = gameRepo.getIncompleteLobbyByUser(userId)
            if (lobby != null) return@run success(Game(lobby.id,lobby.grid,lobby.openingRule.name,lobby.variant.name))
            val match = gameRepo.getMatchByUser(userId)
            if (match != null) {
                val details = gameRepo.getLobby(match.id) ?: throw IllegalStateException("Inconsistent DB state, match only exists if there is a Lobby")
                return@run success(Game(details.id,details.grid,details.openingRule.name,details.variant.name))
            }
            failure(GameValidationError.LobbyNotFound)
        }
    }

    fun fetchLobbyById(lobbyId: Int, id: Int): GameOperationResult {
        return transactionManager.run {
            val gameRepo = it.gameRepository
            val lobby = gameRepo.getLobby(lobbyId) ?: return@run failure(GameValidationError.LobbyNotFound)
            if (lobby.userAId == id || lobby.userBId == id){
                return@run success(Game(lobby.id,lobby.grid,lobby.openingRule.name,lobby.variant.name))
            }
            failure(GameValidationError.UnauthorizedPlayer)
        }

    }

    /**
     * @param persistChanges Function responsible for persisting the changes to storage depending on the result of the
     * operation, and responsible for giving feedback on the outcome.
     * */
    private fun prepareBoard(
        lobby: Lobby,
        moves: List<Move>,
        config: BoardConfig,
        userId: Int,
        persistChanges: (OpeningRuleOperation) -> GameStatus
    ): GameOperationResult {
        return when (val ruleOpener: OpeningRuleResult = gameDomain.ruleOpener(lobby, moves, config, userId)) {
            is Success ->
                success(persistChanges(ruleOpener.value))

            is Failure -> {
                when (val res = ruleOpener.value) {
                    is OpeningRuleError.NoSelectedGoPiece -> failure(GameValidationError.NoSelectedGoPiece)
                    is OpeningRuleError.IncorrectPieceAmount -> failure(GameValidationError.InsufficientPieces(res.validPieces))
                    is OpeningRuleError.InvalidTurn -> failure(GameValidationError.InvalidTurn)
                }

            }
        }
    }

    fun placePieces(config: BoardConfig, lobbyId: Int, userId: Int): GameOperationResult {
        return transactionManager.run {
            val gameRepo = it.gameRepository
            val lobby = gameRepo.getLobby(lobbyId) ?: return@run failure(GameValidationError.LobbyNotFound)
            config.positions.forEach { position ->
                if (gameDomain.checkGridConstraints(position, lobby.grid)) {
                    //Play out of bounds
                    return@run failure(GameValidationError.InvalidPosition)
                }
            }
            //Does user belong to match
            if (!gameDomain.belongsToMatch(lobby, userId))
                return@run failure(GameValidationError.UnauthorizedPlayer)
            //See if game exists Match
            val match = gameRepo.getMatch(lobbyId) ?: return@run failure(GameValidationError.WaitingForOpponent)
            if (match.blackPieceId != null) return@run failure(GameValidationError.BoardAlreadySetup)
            val moves = gameDomain.convertToMoves(
                gameRepo.getPlays(lobbyId)
            )
            val opponent = if (lobby.userAId == userId) {
                lobby.userBId ?: return@run success(AwaitingOpponent(lobbyId,lobby.grid))
            } else lobby.userAId
            //Check if game as ended
            if (match.endTime != null) {
                val gameState = gameRepo.getOutcome(lobbyId)
                return@run if (gameState != null) {//Can be null if the users have not yet selected all go Piece when preparating the board
                    success(GameEnded(gameState.winner, lobby.grid, moves, null,opponent))
                }
                else
                //Should not happen
                    throw IllegalStateException(
                        "Business integrity was not fulfilled, match as endtime but there is no GameOutcome"
                    )
            }

            return@run prepareBoard(lobby, moves, config, userId) { operation ->
                when (operation) {
                    is OpeningRuleOperation.ChoseBlackPiece -> {
                        gameRepo.assignBlackPlayer(lobbyId, userId)
                        val turn = gameDomain.validateTurn(moves, GoPiece.BLACK)
                        GameRunning(lobbyId,lobby.grid, GoPiece.BLACK, turn,moves,opponent)
                    }

                    is OpeningRuleOperation.ChoseWhitePiece -> {
                        val blackId = if (userId == lobby.userAId) lobby.userBId!! else lobby.userAId
                        gameRepo.assignBlackPlayer(lobbyId, blackId)
                        val turn = gameDomain.validateTurn(moves, GoPiece.WHITE)
                        GameRunning(lobbyId,lobby.grid, GoPiece.WHITE, turn,moves,opponent)
                    }

                    is OpeningRuleOperation.AddedPieces -> {
                        gameRepo.storePieces(lobbyId, config.positions)
                        WaitingOpponentPieces(lobbyId,lobby.grid,moves,opponent)
                    }

                    is OpeningRuleOperation.AddedAndChoseBlack -> {
                        gameRepo.storePieces(lobbyId, config.positions)
                        gameRepo.assignBlackPlayer(lobbyId, userId)
                        val turn = gameDomain.validateTurn(moves, GoPiece.BLACK)
                        GameRunning(lobbyId,lobby.grid, GoPiece.BLACK, turn,moves,opponent)
                    }

                    is OpeningRuleOperation.AddedAndChoseWhite -> {
                        gameRepo.storePieces(lobbyId, config.positions)
                        val blackId = if (userId == lobby.userAId) lobby.userBId!! else lobby.userAId
                        gameRepo.assignBlackPlayer(lobbyId, blackId)
                        val turn = gameDomain.validateTurn(moves, GoPiece.WHITE)
                        GameRunning(lobbyId,lobby.grid, GoPiece.WHITE, turn,moves,opponent)
                    }
                }
            }


        }


    }




}
