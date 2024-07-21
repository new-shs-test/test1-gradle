package com.isel.gomokuApi.domain.gameLogic.openingRules

import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Move
import com.isel.gomokuApi.http.model.BoardConfig
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.utils.failure
import com.isel.gomokuApi.utils.success

object SwapTwo : OpeningRule() {

    private const val EMPTY_BOARD = 0
    private const val PLAYER_B_CHOICE = 3
    private const val PLAYER_A_CHOICE = 5

    override fun prepareBoard(
        lobby: Lobby,
        currentMoves: List<Move>,
        desiredConfig: BoardConfig,
        userId: Int
    ): OpeningRuleResult {
        return when (currentMoves.size) {
            EMPTY_BOARD-> {
                when {
                    lobby.userAId != userId -> failure(OpeningRuleError.InvalidTurn)
                    desiredConfig.positions.size == 3 -> success(OpeningRuleOperation.AddedPieces)
                    else -> failure(OpeningRuleError.IncorrectPieceAmount(listOf(3)))
                }
            }

            PLAYER_B_CHOICE -> {
                when {
                    lobby.userBId != userId -> failure(OpeningRuleError.InvalidTurn)
                    desiredConfig.positions.isEmpty() -> success(OpeningRuleOperation.ChoseBlackPiece)//Swap color and play as Black
                    desiredConfig.positions.size == 1 -> success(OpeningRuleOperation.AddedAndChoseWhite)//Add piece and play as white
                    desiredConfig.positions.size == 2 -> success(OpeningRuleOperation.AddedPieces)//Add two pieces and player a chooses color
                    else -> failure(OpeningRuleError.IncorrectPieceAmount(listOf(0,1,2)))
                }
            }
            PLAYER_A_CHOICE -> {
                when{
                    lobby.userAId != userId -> failure(OpeningRuleError.InvalidTurn)
                    desiredConfig.swapColor != null -> {
                        success(
                            if (desiredConfig.swapColor == GoPiece.BLACK) OpeningRuleOperation.ChoseBlackPiece
                            else OpeningRuleOperation.ChoseWhitePiece
                        )
                    }
                    else -> failure(OpeningRuleError.NoSelectedGoPiece)
                }
            }

            else -> throw IllegalStateException("Board should already be opened")
        }
    }

    override fun assignIdOnCreation(userAId: Int, userId: Int): Int? = null
    override fun validateTurn(lobby: Lobby, userId: Int, piecesCount: Int): Pair<Boolean, String> {
       return when(piecesCount){
            EMPTY_BOARD -> Pair(lobby.userAId != userId,"3")
            PLAYER_B_CHOICE -> Pair(lobby.userBId != userId,"012")
            PLAYER_A_CHOICE -> Pair(lobby.userAId != userId,"0")
           else -> {
               throw IllegalStateException("Game should already be running, incorrect validation request")
           }
       }
    }

    //fun placePieces(moves: List<Move>,)
}