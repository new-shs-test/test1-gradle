package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenInfo
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class TokenInfoMapper : RowMapper<TokenInfo> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): TokenInfo? {
        return rs?.let {
            TokenInfo(
                Token(rs.getString(1)),
                rs.getInt(2),
                Instant.fromEpochSeconds(rs.getLong(3)),
                Instant.fromEpochSeconds(rs.getLong(4))
            )
        }
    }
}