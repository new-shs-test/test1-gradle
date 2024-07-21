package com.isel.gomokuApi.repository.model

import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule

data class Lobby(val id: Int, val userAId: Int, val userBId: Int?, val grid:Int, val openingRule: Rule, val variant: Variant)