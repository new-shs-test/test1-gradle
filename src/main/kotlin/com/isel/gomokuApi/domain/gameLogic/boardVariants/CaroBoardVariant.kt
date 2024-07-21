package com.isel.gomokuApi.domain.gameLogic.boardVariants

import com.isel.gomokuApi.domain.model.game.GameState
import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.domain.model.game.Position

class CaroBoardVariant(
    override val moves: List<Move>, override val boardSize: Int,
    override val activeGoPiece: GoPiece
) : BoardVariant() {
    companion object {
       const val NAME: String = "CARO"
    }

    override fun play(pos: Position): GameState {
        val pos = moves.last().position
        return when {
            checkWinner( pos) -> GameState.WINNER
            moves.size == boardSize * boardSize - 1 -> GameState.DRAW
            else -> GameState.RUN
        }
    }

    private fun checkWinner( pos: Position): Boolean {
        val dirs = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(-1, 1)).map { p ->
            val a = countPieces(pos, Pair(p.first, p.second))
            val b = countPieces(pos, Pair(-p.first, p.second))
            if (a.first + b.first + 1 == VICTORY_LINE) !(a.second || b.second)
            else a.first + b.first + 1 > VICTORY_LINE
        }
        return dirs.reduce { f, s -> f || s }
    }
    //Checks for blockage
    private fun countPieces(lastPlay: Position, direction: Pair<Int, Int>, counter: Int = 0): Pair<Int, Boolean> {
        val checkPos = nextPos(lastPlay, direction, boardSize) ?: return Pair(counter, false)
        val checkPosMove = moves.find { m -> m.position == checkPos }
        if (checkPosMove == null) return Pair(counter, false)//pos vazia
        else if (checkPosMove.goPiece == activeGoPiece) return countPieces(checkPos, direction, counter + 1)
        return Pair(counter, true)
    }
}