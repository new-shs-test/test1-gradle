package com.isel.gomokuApi.repository.jdbi.mappers


import com.isel.gomokuApi.domain.model.statistcs.VictoriesRanking
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class VictoriesRankingMapper : RowMapper<VictoriesRanking> {
    override fun map(rs: ResultSet, ctx: StatementContext): VictoriesRanking {
        return VictoriesRanking(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getInt("total_wins"),
            rs.getInt("rank")
        )
    }
}