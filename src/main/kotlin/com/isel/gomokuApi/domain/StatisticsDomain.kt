package com.isel.gomokuApi.domain

import com.isel.gomokuApi.domain.model.statistcs.TimePlayedRanking
import com.isel.gomokuApi.repository.model.TimePlayedRankingDB
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Component
class StatisticsDomain{
        fun convertToTimeRanking(mostTime: List<TimePlayedRankingDB>, converter : (Long) -> Duration ): List<TimePlayedRanking> =
                mostTime.map { TimePlayedRanking(it.id, it.playerName,converter(it.timePlayed).toString(),it.rank) }
        fun pageSetup(page: Int, pageSize: Int): Pair<Int,Int> {
                val start = page * pageSize
                val end = start + pageSize
                return Pair(start,end)
        }
}