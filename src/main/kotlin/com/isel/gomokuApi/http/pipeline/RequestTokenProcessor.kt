package com.isel.gomokuApi.http.pipeline

import com.isel.gomokuApi.domain.AuthenticatedUser
import com.isel.gomokuApi.services.UserServices
import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Component

@Component
class RequestTokenProcessor(
    val userServices: UserServices
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }
        return userServices.getUserByToken(parts[1])?.let {
            AuthenticatedUser(
                it,
                parts[1]
            )
        }
    }

    fun processAuthorizationCookieHeaderValue(cookie: Cookie): AuthenticatedUser? {
        return userServices.getUserByToken(cookie.value)?.let {
            AuthenticatedUser(
                it,
                cookie.value
            )
        }
    }

    companion object {
        const val HTTP_ONLY_KEY = "authToken"
        const val SCHEME = "bearer"
    }
}
