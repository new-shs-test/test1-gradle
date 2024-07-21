package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.game.GameOutcome
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class GameOutcomeMapper : RowMapper<GameOutcome> {
    override fun map(rs: ResultSet?, ctx: StatementContext): GameOutcome? {
        return rs?.let {
            val winner = rs.getInt("winner")
            GameOutcome(
                rs.getInt("match_id"),
                if (winner != 0) winner else null,
                rs.getInt("a_points"),
                rs.getInt("b_points"),
                rs.getLong("duration")
            )
        }
    }
}