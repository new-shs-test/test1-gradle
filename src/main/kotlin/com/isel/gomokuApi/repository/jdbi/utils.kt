package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.repository.jdbi.mappers.*
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

fun Jdbi.configureWithAppRequirements(): Jdbi{

    registerRowMapper(PlayMapper())
    registerRowMapper(LobbyMapper())
    registerRowMapper(GameOutcomeMapper())
    registerRowMapper(MatchMapper())


    registerRowMapper(GlobalStatsDataMapper())
    registerRowMapper(BestPlayerRankingMapper())
    registerRowMapper(DefeatsRankingMapper())
    registerRowMapper(GamesRankingMapper())
    registerRowMapper(VictoriesRankingMapper())
    registerRowMapper(TimePlayedRankingMapper())

    registerRowMapper(UserMapper())
    registerRowMapper(UserInfoMapper())
    registerRowMapper(UserAndTokenModelMapper())
    registerRowMapper(TokenInfoMapper())
    return this

}