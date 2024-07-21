package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.repository.model.Match
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class MatchMapper: RowMapper<Match> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): Match? {
        return rs?.let {
            val endTime = rs.getLong("end_time")
            Match(
                rs.getInt("match_id"),
                rs.getInt("black_id"),
                rs.getLong("start_time"),
                if (endTime != 0L) endTime else null
            )
        }
    }
}