package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.repository.model.GlobalStatsData
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class GlobalStatsDataMapper : RowMapper<GlobalStatsData> {
    override fun map(rs: ResultSet, ctx: StatementContext): GlobalStatsData {
        return GlobalStatsData(
            rs.getLong("total_duration"),
            rs.getInt("total_games"),
            rs.getInt("total_wins")
        )
    }
}