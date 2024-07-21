package com.isel.gomokuApi.repository.localRep.mem

import com.isel.gomokuApi.domain.model.game.GameOutcome
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Match
import com.isel.gomokuApi.repository.model.Play

class GameMem {
    var lobbys = mutableListOf<Lobby>()
    var matches = mutableMapOf<Int,Match>()
    var outcomes = mutableListOf<GameOutcome>()
    var plays = mutableListOf<Play>()
    fun reset() {
        lobbys.clear()
        matches.clear()
        outcomes.clear()
        plays.clear()
    }
}
