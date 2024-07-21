package com.isel.gomokuApi.repository.jdbi.mappers


import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.model.Users.SecurePassword
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class UserMapper : RowMapper<User>{
    override fun map(rs: ResultSet?, ctx: StatementContext?): User? {
        return if (rs == null) null else {
            User(
                rs.getInt(1),
                rs.getString(3),
                rs.getString(4),
                SecurePassword(rs.getString(2))
            )
        }
    }

}