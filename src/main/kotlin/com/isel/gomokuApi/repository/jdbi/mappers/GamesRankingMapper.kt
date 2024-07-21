package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.statistcs.GamesRanking
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class GamesRankingMapper : RowMapper<GamesRanking> {
    override fun map(rs: ResultSet, ctx: StatementContext): GamesRanking {
        return GamesRanking(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getInt("total_games"),
            rs.getInt("rank")
        )
    }
}