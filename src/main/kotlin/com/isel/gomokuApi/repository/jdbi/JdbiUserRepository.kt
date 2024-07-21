package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenInfo
import com.isel.gomokuApi.repository.model.UserInfoData
import com.isel.gomokuApi.repository.UserRepository
import com.isel.gomokuApi.repository.model.UserAndTokenModel
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo


class JdbiUserRepository(private val handle: Handle) : UserRepository {
    override fun getUserInfoById(id: Int): UserInfoData?  {
        return handle.createQuery(
            """
        SELECT 
            u.nickname,
            SUM( CASE WHEN u.id = l.player_a THEN o.a_points ELSE o.b_points END ) AS total_points,
            COUNT(CASE WHEN o.winner IS NOT NULL AND u.id = o.winner THEN 1 END) AS total_wins,
            COUNT(CASE WHEN o.winner IS NULL THEN 1 END) AS total_draws,
            COUNT(CASE WHEN o.winner IS NOT NULL AND o.winner <> u.id THEN 1 END) AS total_losses,
            COUNT(*) AS total_games,
            COALESCE(SUM(o.duration), 0) AS total_time
        FROM users u left join lobby l on u.id = l.player_a or u.id = l.player_b
        left join outcome o on l.id = o.match_id
        WHERE u.id = :id 
        GROUP BY u.nickname

        """.trimIndent()
        ).bind("id", id)
            .mapTo<UserInfoData>()
            .singleOrNull()
    }
    override fun getUserInfoByNickname(nickname: String): UserInfoData? {
        return handle.createQuery(
            """
        SELECT 
            u.nickname,
            SUM(CASE WHEN u.id = l.player_a THEN o.a_points ELSE o.b_points END) AS total_points,
            COUNT(CASE WHEN o.winner IS NOT NULL AND u.id = o.winner THEN 1 END) AS total_wins,
            COUNT(CASE WHEN o.winner IS NULL THEN 1 END) AS total_draws,
            COUNT(CASE WHEN o.winner IS NOT NULL AND o.winner <> u.id THEN 1 END) AS total_losses,
            COUNT(*) AS total_games,
            COALESCE(SUM(o.duration), 0) AS total_time
        FROM users u 
        LEFT JOIN lobby l ON u.id = l.player_a OR u.id = l.player_b
        LEFT JOIN outcome o ON l.id = o.match_id
        where u.nickname = :nickname
        GROUP BY u.nickname
        """.trimIndent()
        )
            .bind("nickname", nickname)
            .mapTo<UserInfoData>()
            .singleOrNull()
    }


    override fun getUserByNickname(nickname: String) =
        handle.createQuery("select * from users where nickname = :nickname")
            .bind("nickname", nickname).mapTo<User>()
            .singleOrNull()

    override fun getUserByEmail(email: String) =
        handle.createQuery("select * from users where email = :email")
            .bind("email", email).mapTo<User>().singleOrNull()

    override fun storeUser(nickname: String, email: String, password: SecurePassword): Int {

        return handle.createUpdate(
            "insert into users (nickname, email, password) values (:nick, :email, :password)"
        )
            .bind("nick", nickname)
            .bind("email", email)
            .bind("password", password.encodedPassword)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    }

    override fun createToken(tokenInfo: TokenInfo) {
        handle.createUpdate(
            "delete from token_info where player_id = :user"
        )
            .bind("user", tokenInfo.userId)
            .execute()

        //Store new token
        handle.createUpdate(
            """
                insert into token_info(token, player_id, created_at, last_used_at)
                    values (:token,:player,:first_used,:last_used)
            """.trimIndent()
        )
            .bind("token", tokenInfo.token.value)
            .bind("player", tokenInfo.userId)
            .bind("first_used", tokenInfo.createdAt.epochSeconds)
            .bind("last_used", tokenInfo.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun searchForTokenByUserId(id: Int): TokenInfo? = handle.createQuery(
        "select * from token_info where player_id = :user"
    ).bind("user", id).mapTo<TokenInfo>().singleOrNull()

    override fun getTokenInfoByToken(validatingToken: Token): Pair<User, TokenInfo>? =
        handle.createQuery(
            """
                select id, nickname,email,password, token, created_at, last_used_at
                    from users as u
                    inner join token_info as tokens
                    on u.id = tokens.player_id
                    where token = :validation_information
            """.trimIndent()
        )
            .bind("validation_information", validatingToken.value)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()?.userAndToken

    override fun updateTokenLastUsed(tokenInfo: TokenInfo, now: Long){
        handle.createUpdate(
            """
            update token_info
                set last_used_at = :last_used_at
                where token = :validation_information
            """.trimIndent()
        )
            .bind("last_used_at",now)
            .bind("validation_information",tokenInfo.token.value)
            .execute()
    }

    override fun removeTokenInfoByToken(validatingToken: Token) {
        handle.createUpdate(
            """
                delete from token_info
                where token = :validation_information
            """.trimIndent()
        ).bind("validation_information",validatingToken.value)
            .execute()
    }

    override fun deleteUser(userId: Int) {
        handle.createUpdate(
            """
                delete from users
                where id = :userId
            """.trimIndent()
        ).bind("userId",userId).execute()
    }


}