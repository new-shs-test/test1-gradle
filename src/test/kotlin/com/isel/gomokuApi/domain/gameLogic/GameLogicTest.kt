package com.isel.gomokuApi.domain.gameLogic

import com.isel.gomokuApi.domain.gameLogic.boardVariants.StandardBoardVariant
import com.isel.gomokuApi.domain.model.game.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GameLogicTest {

    @Test
    fun `horizontal win check`(){
        val moves = listOf<Move>(
                                 Move (Position(1,1), GoPiece.BLACK),
                                 Move (Position(1,2), GoPiece.BLACK),
                                 Move (Position(1,3), GoPiece.BLACK),
                                 Move (Position(1,4), GoPiece.BLACK)

        )
        val game = StandardBoardVariant(moves,15, GoPiece.BLACK)
        assertEquals(GameState.WINNER, game.play(Position(1,5)))
    }

    @Test
    fun `vertical win check`() {
        val moves = listOf<Move>(
            Move (Position(1,1), GoPiece.BLACK),
            Move (Position(2,1), GoPiece.BLACK),
            Move (Position(3,1), GoPiece.BLACK),
            Move (Position(4,1), GoPiece.BLACK),
        )
        val game = StandardBoardVariant(moves,15, GoPiece.BLACK)
        assertEquals(GameState.WINNER, game.play(Position(5,1)))
    }

    @Test
    fun `diagonal 1 win check` () {
        val moves = listOf<Move>(
            Move (Position(1,1), GoPiece.BLACK),
            Move (Position(2,2), GoPiece.BLACK),
            Move (Position(3,3), GoPiece.BLACK),
            Move (Position(4,4), GoPiece.BLACK),
        )
        val game = StandardBoardVariant(moves,15, GoPiece.BLACK)
        assertEquals(GameState.WINNER, game.play(Position(5,5)))
    }

    @Test
    fun `diagonal 2 win check` () {
        val moves = listOf<Move>(
            Move (Position(1,5), GoPiece.BLACK),
            Move (Position(2,4), GoPiece.BLACK),
            Move (Position(3,3), GoPiece.BLACK),
            Move (Position(4,2), GoPiece.BLACK),
        )
        val game = StandardBoardVariant(moves,15, GoPiece.BLACK)
        assertEquals(GameState.WINNER, game.play(Position(5,1)))
    }

@Test
    fun `draw check` (){
        val moves = mutableListOf<Move>()
        for (i in 0 until 15) {
            println(i)
            for (j in 0 until 15) {
                if ((i + j) % 2 == 0) {
                    if (i+j == 28) break
                    moves.add(Move(Position(i, j), GoPiece.BLACK))
                } else {
                    moves.add(Move(Position(i, j), GoPiece.WHITE))
                }
            }
        }
        val game = StandardBoardVariant(moves,15, GoPiece.BLACK)
        val res = game.play(Position(14,14))
        assertEquals(GameState.DRAW, res )
    }



}
