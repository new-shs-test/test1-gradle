package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.repository.model.UserAndTokenModel
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class UserAndTokenModelMapper : RowMapper<UserAndTokenModel> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): UserAndTokenModel? {
        return rs?.let {
            UserAndTokenModel(
                it.getInt(1),
                it.getString(2),
                it.getString(3),
                SecurePassword(it.getString(4)),
                Token(it.getString(5)),
                it.getLong(6),
                it.getLong(7)
            )
        }
    }
}