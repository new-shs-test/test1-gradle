package com.isel.gomokuApi.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import com.isel.gomokuApi.repository.model.TimePlayedRankingDB

class TimePlayedRankingMapper : RowMapper<TimePlayedRankingDB> {
    override fun map(rs: ResultSet, ctx: StatementContext): TimePlayedRankingDB {
        return TimePlayedRankingDB(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getLong("total_time"),
            rs.getInt("rank")
        )
    }
}