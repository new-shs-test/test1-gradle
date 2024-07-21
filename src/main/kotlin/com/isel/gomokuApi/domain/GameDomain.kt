package com.isel.gomokuApi.domain

import com.isel.gomokuApi.domain.gameLogic.boardVariants.BoardVariant
import com.isel.gomokuApi.domain.gameLogic.boardVariants.CaroBoardVariant
import com.isel.gomokuApi.domain.gameLogic.boardVariants.StandardBoardVariant
import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.OpeningRule
import com.isel.gomokuApi.domain.gameLogic.openingRules.OpeningRuleError
import com.isel.gomokuApi.domain.gameLogic.openingRules.OpeningRuleOperation
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule
import com.isel.gomokuApi.domain.model.game.*
import com.isel.gomokuApi.http.model.BoardConfig
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Play
import com.isel.gomokuApi.utils.Either
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component


@Component
class GameDomain {
    private val GRID_NORMAL_SIZE = 19
    private val GRID_SMALL_SIZE = 15
    fun gridValidation(grid: Int) = grid == GRID_SMALL_SIZE || grid == GRID_NORMAL_SIZE


    fun positionValidation(position: Position, moves: List<Move>): Move? =
        moves.firstOrNull { it.position == position }

    /*fun lastPlayerId(playerId: Int, plays: List<Play>): Int? =
        plays.lastOrNull()?.playerId*/

    /**
     * Converts the list of [Play] from the DB to a list of [Move] for the Board to handle. The conversion is needed since
     * the notion of a board ID associated to a play only exists in the database. That information is not need for the
     * Board logic, so it's discarded.
     *
     * @return List of [Move].
     * */
    fun convertToMoves(plays: List<Play>): List<Move>{
        var counter = 1
        return plays.map {
            counter++
            if ((counter % 2) == 0) Move(it.position, GoPiece.BLACK)
            else Move(it.position, GoPiece.WHITE)
        }
    }



    fun openingRuleValidation(openingRule: String): Rule? =
         Rule.values().firstOrNull { it.name == openingRule }


    fun belongsToMatch(lobby: Lobby, userId: Int): Boolean {
        return lobby.userAId == userId || lobby.userBId == userId
    }

    fun createOutcome(gameInfo: Lobby, userId: Int?, startTime: Long, endTime: Instant): GameOutcome {
        val duration = getGameDuration(startTime, endTime)
        return if (userId != null) {
            if (userId == gameInfo.userAId)
                GameOutcome(
                    gameInfo.id,
                    userId,
                    GamePoints.VICTORY,
                    GamePoints.DEFEAT,
                    duration
                ) else {
                GameOutcome(
                    gameInfo.id,
                    userId,
                    GamePoints.DEFEAT,
                    GamePoints.VICTORY,
                    duration
                )
            }
        } else {
            GameOutcome(
                gameInfo.id,
                null,
                GamePoints.DEFEAT,
                GamePoints.DEFEAT,
                duration
            )
        }
    }

    fun getGameDuration(startTime: Long, endTime: Instant): Long {
        val startInstant = Instant.fromEpochSeconds(startTime)
        return endTime.minus(startInstant).inWholeSeconds
    }

    fun determinePiece(blackPlayerId: Int, userId: Int): GoPiece =

            if (blackPlayerId == userId) GoPiece.BLACK else GoPiece.WHITE



    fun getOpeningRule(openingRule: Rule): OpeningRule = OpeningRule.getOpeningRule(openingRule)


    fun makeBoard(variant: Variant, grid: Int, moves: List<Move>, activePiece: GoPiece): BoardVariant =
        when(variant){
            Variant.STANDARD-> StandardBoardVariant(moves,grid,activePiece)
            Variant.CARO -> CaroBoardVariant(moves,grid,activePiece)
        }



    fun checkGridConstraints(position: Position, grid: Int): Boolean =
        position.lin >= grid || position.col >= grid

    fun locateVariant(variant: String): Variant? =
        Variant.values().firstOrNull{it.name == variant}

    /**
     * Makes sure the pieces in the board are placed in the correct order, always switching between [GoPiece.BLACK] and
     * [GoPiece.WHITE].
     *
     * @return True if the [activePiece] is different from the lastPiece of the board.
     * */
    fun validateTurn(moves: List<Move>, activePiece: GoPiece): Boolean {
        return if (moves.isEmpty()){
            activePiece == GoPiece.BLACK
        }else{
            moves.last().goPiece != activePiece
        }
    }

    fun ruleOpener(
        lobby: Lobby,
        moves: List<Move>,
        config: BoardConfig,
        userId: Int
    ): Either<OpeningRuleError, OpeningRuleOperation> =
        OpeningRule.getOpeningRule(lobby.openingRule).prepareBoard(lobby,moves,config,userId)

    fun placePieceTurn(lobby: Lobby, userId: Int, piecesCount: Int): Pair<Boolean,String> =
        getOpeningRule(lobby.openingRule).validateTurn(lobby, userId, piecesCount)

    fun assignIdOnCreation(openingRule: Rule,userAId:Int, userId: Int): Int? =
        getOpeningRule(openingRule).assignIdOnCreation(userAId,userId)



}
