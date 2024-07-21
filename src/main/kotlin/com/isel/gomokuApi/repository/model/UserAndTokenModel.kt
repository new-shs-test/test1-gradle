package com.isel.gomokuApi.repository.model

import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenInfo
import kotlinx.datetime.Instant

data class UserAndTokenModel(
    val id: Int,
    val username: String,
    val email: String,
    val passwordValidation: SecurePassword,
    val tokenValidation: Token,
    val createdAt: Long,
    val lastUsedAt: Long
) {
    val userAndToken: Pair<User, TokenInfo>
        get() = Pair(
            User(id, username, email,passwordValidation),
            TokenInfo(
                tokenValidation,
                id,
                Instant.fromEpochSeconds(createdAt),
                Instant.fromEpochSeconds(lastUsedAt)
            )
        )
}