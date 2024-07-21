package com.isel.gomokuApi.services


import com.isel.gomokuApi.domain.StatisticsDomain
import com.isel.gomokuApi.domain.model.statistcs.BestPlayerRanking
import com.isel.gomokuApi.domain.model.statistcs.BestPlayerRankingOutput
import com.isel.gomokuApi.domain.model.statistcs.GlobalStatistics
import com.isel.gomokuApi.http.model.*
import com.isel.gomokuApi.repository.TransactionManager
import com.isel.gomokuApi.services.utils.TimeParser
import org.springframework.stereotype.Component

@Component
class StatisticServices(
    private val transactionManager: TransactionManager,
    private val statsDomain : StatisticsDomain
) {
    companion object {
        const val URI = "http://localhost:8000/api/statistics/ranking/"
        const val PAGE_SIZE = 10
        const val PAGE_SIZE_ANDROID = 10
    }

    fun getGlobalStats(): GlobalStatistics {
        return transactionManager.run {
            val statsRepo = it.statsRepository
            val stats = statsRepo.fetchGlobalStats()
            val totalDuration = TimeParser().convertToTime(stats.totalHours)
            return@run GlobalStatistics(totalDuration.toString(),stats.totalGames,stats.totalVictories)
        }
    }
    fun getGlobalRanking ( page:Int, nickname: String? ): BestPlayerRankingOutput?{
        return transactionManager.run {
            val statsRepo = it.statsRepository
            val (start, end) = statsDomain.pageSetup(page, PAGE_SIZE_ANDROID)
            val bestPlayers = statsRepo.bestPlayers(start,end)
            if (nickname == null || nickname == "") {
                val nextPage = if (PAGE_SIZE == bestPlayers.size) page+1 else null
                return@run BestPlayerRankingOutput(bestPlayers, nextPage)
            } else {
                var listPlayers = listOf<BestPlayerRanking>()
                var currPage = page
                var currList = bestPlayers
               while (true) {
                   listPlayers += currList
                   val player = currList.find { it.playerName == nickname }
                   if (player != null) {
                       val nextPage = if (PAGE_SIZE == bestPlayers.size) currPage+1 else null
                       return@run BestPlayerRankingOutput(listPlayers, nextPage)
                   }
                   currPage++
                   val (start, end) = statsDomain.pageSetup(currPage, PAGE_SIZE)
                   currList = statsRepo.bestPlayers(start,end)
                   if (currList.isEmpty()) break
               }
            }
            return@run null
        }
    }
    fun getGlobalRanking(page: Int): RankingOutput {
        return transactionManager.run {
            val (start, end) = statsDomain.pageSetup(page, PAGE_SIZE)
            val statsRepo = it.statsRepository
            val bestPlayers = statsRepo.bestPlayers(start,end)
            val mostVictories = statsRepo.mostVictories(start,end)
            val mostGames = statsRepo.mostGames(start,end)
            val mostTime = statsRepo.mostTime(start,end)
            val timeRaking = statsDomain.convertToTimeRanking (mostTime, TimeParser()::convertToTime)
            val mostDefeats = statsRepo.mostDefeats(start,end)
            val rankings = RankingModel(bestPlayers,mostVictories,mostGames,timeRaking,mostDefeats)

            val prevUri = if (page>0) "$URI${page-1}" else null
            val nextUri = if (bestPlayers.size == PAGE_SIZE) "$URI${page+1}" else null
            return@run RankingOutput(rankings, prevUri, nextUri)
        }
    }
}
