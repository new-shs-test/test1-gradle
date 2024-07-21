package com.isel.gomokuApi.domain.gameLogic.openingRules

import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.http.model.BoardConfig
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.utils.Either

sealed class OpeningRuleError {
    /**
     * Players have not yet chosen their game colors.
     * */
    object NoSelectedGoPiece : OpeningRuleError()
    object InvalidTurn : OpeningRuleError()
    class IncorrectPieceAmount(val validPieces: List<Int>) : OpeningRuleError()

}

sealed class OpeningRuleOperation {
    /**
     *
     * */
    object ChoseBlackPiece : OpeningRuleOperation()
    object ChoseWhitePiece : OpeningRuleOperation()
    object AddedPieces : OpeningRuleOperation()
    object AddedAndChoseBlack : OpeningRuleOperation()
    object AddedAndChoseWhite : OpeningRuleOperation()
}

typealias OpeningRuleResult = Either<OpeningRuleError,OpeningRuleOperation>

sealed class OpeningRule {
    /**
     * Also makes turn validations
     * */
    abstract fun prepareBoard(
        lobby: Lobby,
        currentMoves: List<Move>,
        desiredConfig: BoardConfig,
        userId: Int
    ) : OpeningRuleResult

    abstract fun assignIdOnCreation(userAId:Int,userId: Int): Int?

    abstract fun validateTurn(lobby: Lobby,userId: Int,piecesCount : Int): Pair<Boolean, String>


    companion object {
        fun getOpeningRule(openingRule: Rule): OpeningRule =
            when (openingRule) {
                Rule.STANDARD -> StandardOpen
                Rule.SWAP2 -> SwapTwo
            }
    }
}
