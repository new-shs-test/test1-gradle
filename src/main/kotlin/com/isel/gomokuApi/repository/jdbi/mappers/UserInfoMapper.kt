package com.isel.gomokuApi.repository.jdbi.mappers

import com.isel.gomokuApi.repository.model.UserInfoData
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class UserInfoMapper : RowMapper<UserInfoData> {
    override fun map(rs: ResultSet, ctx: StatementContext): UserInfoData {
        return UserInfoData(
            rs.getString("nickname"),
            rs.getInt("total_points"),
            rs.getInt("total_wins"),
            rs.getInt("total_losses"),
            rs.getInt("total_draws"),
            rs.getInt("total_games"),
            rs.getLong("total_time")
        )
    }
}