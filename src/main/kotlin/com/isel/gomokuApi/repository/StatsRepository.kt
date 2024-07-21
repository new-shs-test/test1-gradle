package com.isel.gomokuApi.repository

import com.isel.gomokuApi.domain.model.statistcs.BestPlayerRanking
import com.isel.gomokuApi.domain.model.statistcs.DefeatsRanking
import com.isel.gomokuApi.domain.model.statistcs.GamesRanking
import com.isel.gomokuApi.domain.model.statistcs.VictoriesRanking
import com.isel.gomokuApi.repository.model.GlobalStatsData
import com.isel.gomokuApi.repository.model.TimePlayedRankingDB

interface StatsRepository {
    fun fetchGlobalStats():GlobalStatsData
    fun bestPlayers(start:Int, end: Int): List<BestPlayerRanking>
    fun mostVictories(start:Int, end: Int): List<VictoriesRanking>
    fun mostGames(start:Int, end: Int): List<GamesRanking>
    fun mostTime(start:Int, end: Int): List<TimePlayedRankingDB>
    fun mostDefeats(start:Int, end: Int): List<DefeatsRanking>
}
