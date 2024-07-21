package com.isel.gomokuApi.domain.model.game

sealed class GamePoints {
    companion object{
        const val VICTORY = 20
        const val DEFEAT = 10
    }
}