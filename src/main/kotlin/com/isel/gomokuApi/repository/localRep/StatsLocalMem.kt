package com.isel.gomokuApi.repository.localRep

import com.isel.gomokuApi.domain.model.statistcs.BestPlayerRanking
import com.isel.gomokuApi.domain.model.statistcs.DefeatsRanking
import com.isel.gomokuApi.domain.model.statistcs.GamesRanking
import com.isel.gomokuApi.domain.model.statistcs.VictoriesRanking
import com.isel.gomokuApi.repository.StatsRepository
import com.isel.gomokuApi.repository.model.GlobalStatsData
import com.isel.gomokuApi.repository.model.TimePlayedRankingDB

class StatsLocalMem() : StatsRepository {

    fun reset() {
        TODO()
    }

    override fun fetchGlobalStats(): GlobalStatsData {
        TODO("Not yet implemented")
    }

    override fun bestPlayers(start: Int, end: Int): List<BestPlayerRanking> {
        TODO("Not yet implemented")
    }

    override fun mostVictories(start: Int, end: Int): List<VictoriesRanking> {
        TODO("Not yet implemented")
    }

    override fun mostGames(start: Int, end: Int): List<GamesRanking> {
        TODO("Not yet implemented")
    }

    override fun mostTime(start: Int, end: Int): List<TimePlayedRankingDB> {
        TODO("Not yet implemented")
    }

    override fun mostDefeats(start: Int, end: Int): List<DefeatsRanking> {
        TODO("Not yet implemented")
    }


}
