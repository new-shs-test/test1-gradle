package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.statistcs.BestPlayerRanking
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class BestPlayerRankingMapper : RowMapper<BestPlayerRanking> {
    override fun map(rs: ResultSet, ctx: StatementContext): BestPlayerRanking {
        return BestPlayerRanking(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getInt("total_points"),
            rs.getInt("rank")
        )
    }
}