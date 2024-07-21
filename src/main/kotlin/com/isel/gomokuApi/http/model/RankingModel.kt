package com.isel.gomokuApi.http.model

import com.isel.gomokuApi.domain.model.statistcs.*


class RankingModel (val bestPlayers: List<BestPlayerRanking>,
                    val victories : List<VictoriesRanking>,
                    val mostGames: List<GamesRanking>,
                    val mostTime: List<TimePlayedRanking>,
                    val playerDefeats: List<DefeatsRanking>)

class RankingOutput ( val rankings: RankingModel, val prevPage: String?, val nextPage: String?)
