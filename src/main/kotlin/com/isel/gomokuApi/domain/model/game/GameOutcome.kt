package com.isel.gomokuApi.domain.model.game

data class GameOutcome(
    val id: Int,
    val winner: Int?,
    val playerAPoints: Int,
    val playerBPoints: Int,
    val duration: Long
)
