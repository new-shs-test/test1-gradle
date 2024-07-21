package com.isel.gomokuApi.http.model

import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Position

data class BoardConfig(val positions: List<Position>, val swapColor: GoPiece? = null)
