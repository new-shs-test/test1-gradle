package com.isel.gomokuApi.domain.gameLogic.boardVariants

import com.isel.gomokuApi.domain.model.game.GameState
import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Position


sealed class BoardVariant {
    abstract val moves : List<Move>
    abstract val boardSize : Int
    abstract val activeGoPiece : GoPiece


    companion object {
        const val VICTORY_LINE = 5
    }
    abstract fun play(pos: Position): GameState

    fun nextPos(pos: Position, dir: Pair<Int, Int>, boardSize: Int): Position? {
        val line = pos.lin + dir.first
        if (line < 0 || line >= boardSize) return null
        val column = pos.col + dir.second
        if (column < 0 || column >= boardSize) return null
        return Position(line, column)
    }

}



