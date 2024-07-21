package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.game.GoPiece
import com.isel.gomokuApi.domain.model.game.Position
import com.isel.gomokuApi.repository.model.Play
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class PlayMapper : RowMapper<Play> {
    override fun map(rs: ResultSet, ctx: StatementContext): Play {
        return Play(
            rs.getInt("match_id"),
            Position(
                rs.getInt("line"),
                rs.getInt("col")
            ),
            //GoPiece.valueOf(rs.getString("go_piece"))
        )
    }
}