package com.isel.gomokuApi.domain.gameLogic.openingRules

import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.http.model.BoardConfig
import com.isel.gomokuApi.repository.model.Lobby

object StandardOpen : OpeningRule() {
    override fun prepareBoard(
        lobby: Lobby,
        currentMoves: List<Move>,
        desiredConfig: BoardConfig,
        userId: Int
    ): OpeningRuleResult {
        throw IllegalStateException("Standard rule set does not need preparation!")
    }

    override fun assignIdOnCreation(userAId: Int, userId: Int): Int? = userAId
    override fun validateTurn(lobby: Lobby, userId: Int, piecesCount: Int): Pair<Boolean, String>  =
        if (piecesCount % 2 == 0) Pair(lobby.userAId == userId,"1") else Pair(lobby.userBId == userId,"1")
}
