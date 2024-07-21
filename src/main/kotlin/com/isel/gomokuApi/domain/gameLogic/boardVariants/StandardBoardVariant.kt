package com.isel.gomokuApi.domain.gameLogic.boardVariants

import com.isel.gomokuApi.domain.model.game.GameState
import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.domain.model.game.Position

class StandardBoardVariant(override val moves: List<Move>, override val boardSize: Int, override val activeGoPiece: GoPiece) :
    BoardVariant() {
    companion object {
        const val NAME: String = "STANDARD"
    }
    override fun play(pos: Position): GameState {
        val addedPlay = moves + Move(pos,activeGoPiece)
        return when {
            addedPlay.size == boardSize * boardSize  -> GameState.DRAW
            checkWinner(addedPlay) -> GameState.WINNER
            else -> GameState.RUN
        }
    }

    private fun checkWinner(moves: List<Move>): Boolean {
        val plMoves = moves.filter { it.goPiece == activeGoPiece }.map { it.position }
        val pos = moves.last().position
        val addLastPlay = 1
        return checkWinCondition(countPieces(plMoves, pos, Pair(0, 1)) + countPieces(plMoves, pos, Pair(0, -1)) + addLastPlay)
                ||checkWinCondition(countPieces(plMoves, pos, Pair(1, 0)) + countPieces(plMoves, pos, Pair(-1, 0)) + addLastPlay)
                ||checkWinCondition(countPieces(plMoves, pos, Pair(1, 1)) + countPieces(plMoves, pos, Pair(-1, -1)) + addLastPlay)
                ||checkWinCondition(countPieces(plMoves, pos, Pair(-1, 1)) + countPieces(plMoves, pos, Pair(1, -1)) + addLastPlay)
    }

    private fun checkWinCondition(int: Int): Boolean =
        int >= VICTORY_LINE
    private fun countPieces(moves: List<Position>, lastPlay: Position, direction: Pair<Int,Int>, counter: Int = 0): Int{
        val checkPos : Position = nextPos(lastPlay, direction, boardSize) ?: return counter
        val position = moves.find { move -> move == checkPos } ?: return counter
        return countPieces(moves, position, direction, counter + 1)
    }
}

