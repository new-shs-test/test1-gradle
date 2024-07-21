package com.isel.gomokuApi.domain.model.game

enum class GoPiece {
    BLACK, WHITE;

    override fun toString(): String {
        return this.name
    }
}