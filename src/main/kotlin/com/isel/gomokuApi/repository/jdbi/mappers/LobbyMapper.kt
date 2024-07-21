package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule
import com.isel.gomokuApi.repository.model.Lobby
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class LobbyMapper : RowMapper<Lobby> {
    override fun map(rs: ResultSet?, ctx: StatementContext): Lobby? {
        return rs?.let {
            val playerB = rs.getInt("player_b")
            Lobby(
                rs.getInt("id"),
                rs.getInt("player_a"),
                if (playerB != 0) playerB else null,
                rs.getInt("grid"),
                Rule.valueOf(rs.getString("opening_rules")),
                Variant.valueOf(rs.getString("variant"))
            )
        }
    }
}
