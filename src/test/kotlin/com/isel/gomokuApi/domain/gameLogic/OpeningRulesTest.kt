package com.isel.gomokuApi.domain.gameLogic

import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.domain.model.game.Position
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OpeningRulesTest {

    @Nested
    inner class Swap2Test {
        @Test
        fun `can validate a ready to play board`() {
            //given: A board with enough pieces to start in swap2 in the first scenario
            val moves = listOf(
                Move(Position(5,5),GoPiece.BLACK),
                Move(Position(3,5),GoPiece.WHITE),
                Move(Position(6,5),GoPiece.BLACK)
            )
        }
    }
}