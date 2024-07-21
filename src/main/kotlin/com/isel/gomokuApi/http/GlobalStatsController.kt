package com.isel.gomokuApi.http

import com.isel.gomokuApi.http.model.Problem
import com.isel.gomokuApi.http.model.StatusCode
import com.isel.gomokuApi.http.model.Uris
import com.isel.gomokuApi.services.StatisticServices
import jakarta.websocket.server.PathParam
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GlobalStatsController(private val statisticServices : StatisticServices) {
    @GetMapping(Uris.Statistics.GLOBAL)
    fun generalStatistics(): ResponseEntity<*> {
        return ResponseEntity.status(StatusCode.OK).body(mapOf("gameStats" to statisticServices.getGlobalStats() ) )
    }

    @GetMapping(Uris.Statistics.RANKING)
    fun generalRankings(@PathVariable page: Int): ResponseEntity<*> {
        return ResponseEntity.status(StatusCode.OK)
            .body(statisticServices.getGlobalRanking(page))
    }

    @GetMapping(Uris.Statistics.RANKING_ANDROID)
    fun getRankings(@PathVariable page: Int, @RequestParam nickname: String): ResponseEntity<*> {
        val res = statisticServices.getGlobalRanking(page, nickname)
        return  if (res != null )  ResponseEntity.status(StatusCode.OK).body(res)
        else Problem.response(StatusCode.NOT_FOUND, Problem.resourceNotFound)
    }
}