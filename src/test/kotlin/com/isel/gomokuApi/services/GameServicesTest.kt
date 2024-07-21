package com.isel.gomokuApi.services

import com.isel.gomokuApi.domain.GameDomain
import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule
import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Position
import com.isel.gomokuApi.http.model.BoardConfig
import com.isel.gomokuApi.repository.localRep.GamesLocalMem
import com.isel.gomokuApi.repository.localRep.LocalMemTransactionManager
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Match
import com.isel.gomokuApi.repository.model.Play
import com.isel.gomokuApi.services.model.*
import com.isel.gomokuApi.utils.Either
import com.isel.gomokuApi.utils.TestClock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.*

class GameServicesTest {
    private val testClock = TestClock()
    private val services = createGameServices(GameDomain(), testClock)

    @AfterTest
    fun `wipe data`() {
        (transactionManager.transaction.gameRepository as GamesLocalMem).reset()
    }

    @Test
    fun `can open lobby, join lobby and start game`() {
        //given: The desired Game rules

        //when: Asking to start a game
        var newGameResult = services.startGame(GRID_1, STANDARD_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)
        val lobbyOpened = when (newGameResult) {
            is Either.Left -> fail(newGameResult.value.toString())
            is Either.Right -> newGameResult.value
        }

        //then: The user waits for another player wanting the same rules
        assertIs<AwaitingOpponent>(lobbyOpened)

        //when: The same user asks for a new game
        newGameResult = services.startGame(GRID_1, STANDARD_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)
        val waitingOpponent = when (newGameResult) {
            is Either.Left -> newGameResult.value
            is Either.Right -> fail("Should not have been allowed to create new lobby")
        }
        //then: Should fail because he is already waiting for an opponent
        assertIs<GameValidationError.WaitingForOpponent>(waitingOpponent)

        //when: A second user tries to start a game with the same configuration

        newGameResult = services.startGame(GRID_1, STANDARD_OPENING_RULE.name, STANDARD_VARIANT.name, user2.id)

        val joinedGame = when (newGameResult) {
            is Either.Left -> fail("Should have joined opened game" + newGameResult.value)
            is Either.Right -> newGameResult.value
        }

        //then: The user is informed that he as joined an opened game
        assertIs<WaitingOpponentPieces>(joinedGame)

        //when: Player 1 tries to start a game, it will be informed that he is in
        //an ongoing match instead of waiting for an opponent

        newGameResult = services.startGame(GRID_1, STANDARD_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)

        val gameAlreadyStarted = when (newGameResult) {
            is Either.Left -> newGameResult.value
            is Either.Right -> fail("Should be informed the game is already on going")
        }

        assertIs<GameValidationError.UserAlreadyInGame>(gameAlreadyStarted)

        newGameResult = services.matchState(lobbyOpened.lobbyId, user1.id)

        val isPlayerBlack = when(newGameResult){
            is Either.Left -> fail("Should get match state but failed with: ${newGameResult.value}")
            is Either.Right -> newGameResult.value
        }
        assertIs<GameRunning>(isPlayerBlack)
        assertEquals(isPlayerBlack.playerPiece,GoPiece.BLACK)
    }

    @Test
    fun `can only quit existant game`() {
        //given: A gameId that does not exist
        val lobbyId = 20

        //when: Trying to quit that game
        val failedToQuit = services.quitGame(lobbyId, user1.id)
        val result = when (failedToQuit) {
            is Either.Left -> failedToQuit.value
            is Either.Right -> fail("Shouldn't be able to quit a game that does not exist")
        }
        //then: User failed to quit a game that does not exist

        assertIs<GameValidationError.LobbyNotFound>(result)

    }

    @Test
    fun `can have multiple waiting lobby's, quit and join other lobby`() {

        //when: Asking to start games with different configurations
        val lobbys = listOf<GameOperationResult>(
            services.startGame(GRID_1, STANDARD_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id),
            services.startGame(GRID_2, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user2.id),
            services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user3.id)
        ).map {
            (it as Either.Right).value
        }
        //Then: All should be waiting for opponent
        lobbys.forEach {
            assertIs<AwaitingOpponent>(it)
        }

        val gameToQuit = (lobbys[0] as AwaitingOpponent).lobbyId

        //When: A player tries to quit a game where he does not belong
        val notAuthorized = services.quitGame(gameToQuit, user2.id)

        val failedAccess = when (notAuthorized) {
            is Either.Left -> notAuthorized.value
            is Either.Right -> fail("User should not be able to quit a game he does not belong to")
        }

        assertIs<GameValidationError.UnauthorizedPlayer>(failedAccess)
        //when: User authorized user tries to quit a game, he's in
        val gameQuit = services.quitGame(gameToQuit, user1.id)

        val closedLobby = when (gameQuit) {
            is Either.Left -> fail("User should have been able to quit")
            is Either.Right -> gameQuit.value
        }
        //then: The lobby is closed
        assertIs<LobbyClosed>(closedLobby)

        //when: The user that quit the game tries to join an open lobby

        val startGame = services.startGame(GRID_2, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)

        val gameStarted = when (startGame) {
            is Either.Left -> fail("User should have been able to join second user game")
            is Either.Right -> startGame.value
        }
        //then: Joins an open lobby with success and the game is started

        assertIs<WaitingOpponentPieces>(gameStarted)


    }

    @Test
    fun `can start a game and make a play`() {
        //given: A game started and joined by two players with standard rules
        initiateGame()

        //when: user1 makes a valid play
        var playResult = services.makePlay(Position(0, 0), mock_lobby.id, user1.id)

        val play1 = when (playResult) {
            is Either.Left -> fail("Play should be valid but failed with:" + playResult.value)
            is Either.Right -> playResult.value
        }
        //then: Should inform of success

        assertIs<PlayMade>(play1)

        //when: user1 tries to play again
        playResult = services.makePlay(Position(0, 0), mock_lobby.id, user1.id)

        val invalidTurn = when (playResult) {
            is Either.Left -> playResult.value
            is Either.Right -> fail("Play should be invalid since its no longer user1 turn")
        }
        //Then: Should be notified that user must wait for his turn
        assertIs<GameValidationError.InvalidTurn>(invalidTurn)

        //when:user 2 trie to play a game that does not exist

        playResult = services.makePlay(Position(1, 0), mock_lobby.id - 1, user2.id)

        val noGame = when (playResult) {
            is Either.Left -> playResult.value
            is Either.Right -> fail("Should be notified that game does not exist")
        }

        //then: Shouldn't be able to play in a non-existant game
        assertIs<GameValidationError.LobbyNotFound>(noGame)

        //when: user 2 tries to play in an occupied position

        playResult = services.makePlay(Position(0, 0), mock_lobby.id, user2.id)

        val invalidPos = when (playResult) {
            is Either.Left -> playResult.value
            is Either.Right -> fail("Should not have been able to play in a occupied position(0,0)")
        }
        //then: Should get invalid position error
        assertIs<GameValidationError.InvalidPosition>(invalidPos)

        //when: user 2 makes a valid play
        playResult = services.makePlay(Position(1, 0), mock_lobby.id, user2.id)
        val canAlsoPlay = when (playResult) {
            is Either.Left -> fail("Play should have been succesfull but failed: " + playResult.value)
            is Either.Right -> playResult.value
        }

        assertIs<PlayMade>(canAlsoPlay)
    }

    @Test
    fun `can get a win`() {
        //given: A board with only one more move for player1 win, and it is his turn
        prepareWin()

        //when: player 1 makes the winning play
        var playResult = services.makePlay(Position(4, 0), mock_lobby.id, user1.id)
        val winningPlay = when (playResult) {
            is Either.Left -> fail("Player should have been able to win but failed: " + playResult.value)
            is Either.Right -> playResult.value
        }

        //then: should create an outcome with player1 as winner
        assertIs<GameEnded>(winningPlay)
        assertEquals(user1.id, winningPlay.winner)

        //when: player 2 still tries to make a play

        playResult = services.makePlay(Position(0, 2), mock_lobby.id, user2.id)
        val playBlocked = when (playResult) {
            is Either.Left -> fail("Should have been informed of opponent win and not cause am error")
            is Either.Right -> playResult.value
        }
        //Then: Should see it as lost and it was not a draw
        assertIs<GameEnded>(playBlocked)
        assertNotNull(playBlocked.winner)
        assertNotEquals(user2.id, playBlocked.winner)
    }

    @Test
    fun `can give up and concede win to opponent`() {
        //given: A game started
        initiateGame()
        //when: player 2 quits

        var quitOperation = services.quitGame(mock_lobby.id, user2.id)
        val playerOneWins = when (quitOperation) {
            is Either.Left -> fail("Player should have been able to quit but failed: " + quitOperation.value)
            is Either.Right -> quitOperation.value
        }
        //Then: the game as ended
        assertIs<GameEnded>(playerOneWins)
        //Then: User 1 is the winner
        assertNotEquals(user2.id, playerOneWins.winner)
        assertEquals(user1.id, playerOneWins.winner)

        //When: player 1 tries to play again

        quitOperation = services.makePlay(Position(0, 0), mock_lobby.id, user1.id)
        val gameEnded = when (quitOperation) {
            is Either.Left -> fail("Should be returned finished game state and not an error: " + quitOperation.value)
            is Either.Right -> quitOperation.value
        }
        //Then: Gets notified of finished game
        assertIs<GameEnded>(gameEnded)
        assertEquals(playerOneWins.winner, gameEnded.winner)

        //when: player 1 tries to quit

        quitOperation = services.quitGame(mock_lobby.id, user1.id)
        val playerOneAlreadyWon = when (quitOperation) {
            is Either.Left -> fail("Player should be notified that it as already won but error occurred: " + quitOperation.value)
            is Either.Right -> quitOperation.value
        }

        //then:Gets notified again that he as already won

        assertIs<GameEnded>(playerOneAlreadyWon)
        assertEquals(gameEnded.winner, playerOneAlreadyWon.winner)

    }

    @Nested
    inner class Swap2Context {
        @Test
        fun `can open lobby, place a piece and start playing as white`() {
            //given: Two players

            //when: Both successfully join the same lobby
            var joinOp = services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)
            val user1Joined = when (joinOp) {
                is Either.Left -> fail(joinOp.value.toString())
                is Either.Right -> joinOp.value
            }
            //then: User A waits for another player wanting the same rules
            assertIs<AwaitingOpponent>(user1Joined)

            joinOp = services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user2.id)
            val user2Joined = when (joinOp) {
                is Either.Left -> fail(joinOp.value.toString())
                is Either.Right -> joinOp.value
            }
            //then: User B waits for another player wanting the same rules
            assertIs<WaitingOpponentPieces>(user2Joined)
            //then: The lobby id should be equal
            assertEquals(user1Joined.lobbyId, user2Joined.lobbyId)

            //when: any user tries to do a play
            val triedPlay = services.makePlay(Position.randomPosition(GRID_1), user1Joined.lobbyId, user1.id)
            val failedPlay = when (triedPlay) {
                is Either.Left -> triedPlay.value
                is Either.Right -> fail("Should not have been able to make a gamePlay without placing the setup pieces")
            }
            //then: is informed the board as not yet been prepared
            assertIs<GameValidationError.BoardNotOpen>(failedPlay)

            //when: Player B tries to place pieces before player A
            val invalidTurnOp = services.placePieces(
                BoardConfig(listOf(Position.randomPosition(GRID_1))), user1Joined.lobbyId,
                user2.id
            )
            val invalidTurn = when (invalidTurnOp) {
                is Either.Left -> invalidTurnOp.value
                is Either.Right -> fail("It should identify its player A turn")
            }
            assertIs<GameValidationError.InvalidTurn>(invalidTurn)

            //when: Player A places pieces

            val initialPlacementOp = services.placePieces(
                BoardConfig(
                    listOf(
                        Position(0, 0),
                        Position(0, 1),
                        Position(0, 2)
                    ),
                ),
                user1Joined.lobbyId,
                user1.id
            )
            val initialPlacement = when (initialPlacementOp) {
                is Either.Left -> fail("Should have been a success but instead failed with: ${initialPlacementOp.value}")
                is Either.Right -> initialPlacementOp.value
            }
            //then: Should wait for player B decision

            assertIs<WaitingOpponentPieces>(initialPlacement)

            //when: Player B decides to play as white and places a second white stone

            val desiresWhite = services.placePieces(
                BoardConfig(
                    listOf(
                        Position(0,3)
                    )
                ),user1Joined.lobbyId
                , user2.id
            )
            val playsAsWhite = when(desiresWhite){
                is Either.Left -> fail("Should have been a success but instead failed with: ${desiresWhite.value}")
                is Either.Right -> desiresWhite.value
            }
            //then:Should be informed of game start
            assertIs<GameRunning>(playsAsWhite)
            assertEquals(playsAsWhite.lobbyId, user1Joined.lobbyId)
            assertEquals(playsAsWhite.playerPiece,GoPiece.WHITE)

           //when: Player A tries to know is piece color

            val shouldBeBlack = services.matchState(user1Joined.lobbyId, user1.id)

            val black = when(shouldBeBlack){
                is Either.Left -> fail("Should have been a success but instead failed with: ${shouldBeBlack.value}")
                is Either.Right -> shouldBeBlack.value
            }
            //then: Should be informed the game is running and plays as Black

            assertIs<GameRunning>(black)
            assertEquals(user1Joined.lobbyId,black.lobbyId)
            assertEquals(GoPiece.BLACK,black.playerPiece)
        }

        @Test
        fun `can open lobby, swap color and start playing as black`() {
            //given: Two players

            //when: Both successfully join the same lobby
            var joinOp = services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)
            val user1Joined = when (joinOp) {
                is Either.Left -> fail(joinOp.value.toString())
                is Either.Right -> joinOp.value
            }
            //then: User A waits for another player wanting the same rules
            assertIs<AwaitingOpponent>(user1Joined)

            joinOp = services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user2.id)
            val user2Joined = when (joinOp) {
                is Either.Left -> fail(joinOp.value.toString())
                is Either.Right -> joinOp.value
            }
            //then: User B joins the lobby
            assertIs<WaitingOpponentPieces>(user2Joined)
            //then: The lobby id should be equal
            assertEquals(user1Joined.lobbyId, user2Joined.lobbyId)

            //when: any user tries to do a play
            val triedPlay = services.makePlay(Position.randomPosition(GRID_1), user1Joined.lobbyId, user1.id)
            val failedPlay = when (triedPlay) {
                is Either.Left -> triedPlay.value
                is Either.Right -> fail("Should not have been able to make a gamePlay without placing the setup pieces")
            }
            //then: is informed the board as not yet been prepared
            assertIs<GameValidationError.BoardNotOpen>(failedPlay)

            //when: Player B tries to place pieces before player A
            val invalidTurnOp = services.placePieces(
                BoardConfig(listOf(Position.randomPosition(GRID_1))), user1Joined.lobbyId,
                user2.id
            )
            val invalidTurn = when (invalidTurnOp) {
                is Either.Left -> invalidTurnOp.value
                is Either.Right -> fail("It should identify its player A turn")
            }
            assertIs<GameValidationError.InvalidTurn>(invalidTurn)

            //when: Player A places pieces

            val initialPlacementOp = services.placePieces(
                BoardConfig(
                    listOf(
                        Position(0, 0),
                        Position(0, 1),
                        Position(0, 2)
                    ),
                ),
                user1Joined.lobbyId,
                user1.id
            )
            val initialPlacement = when (initialPlacementOp) {
                is Either.Left -> fail("Should have been a success but instead failed with: ${initialPlacementOp.value}")
                is Either.Right -> initialPlacementOp.value
            }
            //then: Should wait for player B decision

            assertIs<WaitingOpponentPieces>(initialPlacement)

            //when: Player B decides to play as white and places a second white stone

            val desiresWhite = services.placePieces(
                BoardConfig(
                    emptyList(),GoPiece.BLACK
                ),user1Joined.lobbyId
                , user2.id
            )
            val playsAsWhite = when(desiresWhite){
                is Either.Left -> fail("Should have been a success but instead failed with: ${desiresWhite.value}")
                is Either.Right -> desiresWhite.value
            }
            //then:Should be informed of game start
            assertIs<GameRunning>(playsAsWhite)
            assertEquals(user1Joined.lobbyId,playsAsWhite.lobbyId)
            assertEquals(GoPiece.BLACK,playsAsWhite.playerPiece)

            //when: Player A tries to know is piece color

            val shouldBeBlack = services.matchState(user1Joined.lobbyId, user1.id)

            val black = when(shouldBeBlack){
                is Either.Left -> fail("Should have been a success but instead failed with: ${shouldBeBlack.value}")
                is Either.Right -> shouldBeBlack.value
            }
            //then: Should be informed the game is running and plays as Black

            assertIs<GameRunning>(black)
            assertEquals(user1Joined.lobbyId,black.lobbyId)
            assertEquals(black.playerPiece,GoPiece.WHITE)

            //when: player a tries to make a play

            val triedPlayWhite = services.makePlay(Position.randomPosition(GRID_1), user1Joined.lobbyId, user1.id)
            val playedAsWhite = when (triedPlayWhite) {
                is Either.Left -> fail(
                    "Should have been able to make a play since he is white but faile with:" +
                            "${triedPlayWhite.value}"
                )
                is Either.Right -> triedPlayWhite.value
            }

            assertIs<PlayMade>(playedAsWhite)
        }

        @Test
        fun `can open lobby, add pieces and pass decision to player A`() {
            //given: Two players

            //when: Both successfully join the same lobby
            var joinOp = services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user1.id)
            val user1Joined = when (joinOp) {
                is Either.Left -> fail(joinOp.value.toString())
                is Either.Right -> joinOp.value
            }
            //then: User A waits for another player wanting the same rules
            assertIs<AwaitingOpponent>(user1Joined)

            joinOp = services.startGame(GRID_1, SWAP2_OPENING_RULE.name, STANDARD_VARIANT.name, user2.id)
            val user2Joined = when (joinOp) {
                is Either.Left -> fail(joinOp.value.toString())
                is Either.Right -> joinOp.value
            }
            //then: User B joins the lobby.
            assertIs<WaitingOpponentPieces>(user2Joined)
            //then: The lobby id should be equal
            assertEquals(user1Joined.lobbyId, user2Joined.lobbyId)

            //when: any user tries to do a play
            val triedPlay = services.makePlay(Position.randomPosition(GRID_1), user1Joined.lobbyId, user1.id)
            val failedPlay = when (triedPlay) {
                is Either.Left -> triedPlay.value
                is Either.Right -> fail("Should not have been able to make a gamePlay without placing the setup pieces")
            }
            //then: is informed the board as not yet been prepared
            assertIs<GameValidationError.BoardNotOpen>(failedPlay)

            //when: Player B tries to place pieces before player A
            val invalidTurnOp = services.placePieces(
                BoardConfig(listOf(Position.randomPosition(GRID_1))), user1Joined.lobbyId,
                user2.id
            )
            val invalidTurn = when (invalidTurnOp) {
                is Either.Left -> invalidTurnOp.value
                is Either.Right -> fail("It should identify its player A turn")
            }
            assertIs<GameValidationError.InvalidTurn>(invalidTurn)

            //when: Player A places pieces

            val initialPlacementOp = services.placePieces(
                BoardConfig(
                    listOf(
                        Position(0, 0),
                        Position(0, 1),
                        Position(0, 2)
                    ),
                ),
                user1Joined.lobbyId,
                user1.id
            )
            val initialPlacement = when (initialPlacementOp) {
                is Either.Left -> fail("Should have been a success but instead failed with: ${initialPlacementOp.value}")
                is Either.Right -> initialPlacementOp.value
            }
            //then: Should wait for player B decision

            assertIs<WaitingOpponentPieces>(initialPlacement)

            //when: Player B decides to play as white and places a second white stone

            val desiresWhite = services.placePieces(
                BoardConfig(
                    emptyList(),GoPiece.BLACK
                ),user1Joined.lobbyId
                , user2.id
            )
            val playsAsWhite = when(desiresWhite){
                is Either.Left -> fail("Should have been a success but instead failed with: ${desiresWhite.value}")
                is Either.Right -> desiresWhite.value
            }
            //then:Should be informed of game start
            assertIs<GameRunning>(playsAsWhite)
            assertEquals(user1Joined.lobbyId,playsAsWhite.lobbyId)
            assertEquals(GoPiece.BLACK,playsAsWhite.playerPiece)

            //when: Player A tries to know is piece color

            val shouldBeBlack = services.matchState(user1Joined.lobbyId, user1.id)

            val black = when(shouldBeBlack){
                is Either.Left -> fail("Should have been a success but instead failed with: ${shouldBeBlack.value}")
                is Either.Right -> shouldBeBlack.value
            }
            //then: Should be informed the game is running and plays as Black

            assertIs<GameRunning>(black)
            assertEquals(user1Joined.lobbyId,black.lobbyId)
            assertEquals(black.playerPiece,GoPiece.WHITE)

            //when: player a tries to make a play

            val triedPlayWhite = services.makePlay(Position.randomPosition(GRID_1), user1Joined.lobbyId, user1.id)
            val playedAsWhite = when (triedPlayWhite) {
                is Either.Left -> fail(
                    "Should have been able to make a play since he is white but faile with:" +
                            "${triedPlayWhite.value}"
                )
                is Either.Right -> triedPlayWhite.value
            }

            assertIs<PlayMade>(playedAsWhite)
        }
    }


    companion object {
        private val user1 = User(0, "Test1", "Test", SecurePassword("encoded"))
        private val user2 = User(1, "Test2", "Test2", SecurePassword("encoded"))
        private val user3 = User(2, "Test3", "Test3", SecurePassword("encoded"))
        private const val GRID_1 = 15
        private const val GRID_2 = 19
        private val STANDARD_OPENING_RULE = Rule.STANDARD
        private val SWAP2_OPENING_RULE = Rule.SWAP2
        private val STANDARD_VARIANT = Variant.STANDARD

        private val mock_lobby = Lobby(0, user1.id, user2.id, GRID_1, STANDARD_OPENING_RULE, STANDARD_VARIANT)
        private val mock_match = Match(0, user1.id, 10000L, null)

        /**
         * Opens a game between user1 and user2 where user1 can win with one more move
         * */
        private fun prepareWin() {
            initiateGame()
            (transactionManager.transaction.gameRepository as GamesLocalMem).gameMem.plays =
                mutableListOf(
                    Play(mock_lobby.id, Position(0, 0)),
                    Play(mock_lobby.id, Position(5, 5)),
                    Play(mock_lobby.id, Position(1, 0)),
                    Play(mock_lobby.id, Position(5, 6)),
                    Play(mock_lobby.id, Position(2, 0)),
                    Play(mock_lobby.id, Position(5, 7)),
                    Play(mock_lobby.id, Position(3, 0)),
                    Play(mock_lobby.id, Position(5, 1))
                )
        }

        /**
         * Opens a game between user1 and user2
         * */
        private fun initiateGame() {
            (transactionManager.transaction.gameRepository as GamesLocalMem).gameMem.lobbys =
                mutableListOf(mock_lobby)
            (transactionManager.transaction.gameRepository as GamesLocalMem).gameMem.matches =
                mutableMapOf(0 to mock_match)
        }

        private fun createGameServices(domain: GameDomain, clock: TestClock) = GameServices(
            transactionManager,
            domain,
            clock
        )

        private val transactionManager = LocalMemTransactionManager()

    }
}