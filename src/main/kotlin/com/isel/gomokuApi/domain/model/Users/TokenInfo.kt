package com.isel.gomokuApi.domain.model.Users

import com.isel.gomokuApi.domain.model.Users.Token
import kotlinx.datetime.Instant

data class TokenInfo(val token: Token, val userId: Int, val createdAt : Instant, val lastUsedAt: Instant)
