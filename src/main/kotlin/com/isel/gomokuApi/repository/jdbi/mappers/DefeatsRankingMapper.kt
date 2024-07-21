package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.statistcs.DefeatsRanking
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class DefeatsRankingMapper : RowMapper<DefeatsRanking> {
    override fun map(rs: ResultSet, ctx: StatementContext): DefeatsRanking {
        return DefeatsRanking(
            rs.getInt("id"),
            rs.getString("nickname"), // Use "nickname" em vez de "playerName"
            rs.getInt("total_games"),    // Use "total_games" em vez de "defeats"
            rs.getInt("rank")
        )
    }
}