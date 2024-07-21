package com.isel.gomokuApi.repository.model

import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Position

data class Play (val matchId : Int, val position: Position)