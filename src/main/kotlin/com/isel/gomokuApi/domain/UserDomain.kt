package com.isel.gomokuApi.domain

import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenEncoder
import com.isel.gomokuApi.domain.model.Users.TokenInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class UserDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
) {
    init {
        require(tokenSizeInBytes > 0)
        require(tokenTtl.isPositive())
        require(tokenRollingTtl.isPositive())
    }
}

data class User(
    val id: Int,
    val nickname: String,
    val email: String,
    val passwordEncoded: SecurePassword
)

data class AuthenticatedUser(val user: User, val token: String)

@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
    private val config: UserDomainConfig,
    private val tokenEncoder: TokenEncoder
) {
    private val EMAIL_LOCAL_PART_SIZE = 64
    fun checkPassword(password: String): Boolean = password.contains("[0-9]".toRegex())
            && password.contains("[a-z]".toRegex()) && password.contains("[A-Z]".toRegex())
            && password.length > 6 && password.contains("[^A-Za-z0-9]".toRegex())


    private val emailRegex = "^[\\w\\.-]+@[a-zA-Z\\d\\.-]+\\.[a-zA-Z]{2,}\$"

    /*
        Allowed characters: letters (a-z), numbers, underscores, periods, and dashes.
         An underscore, period, or dash must be followed by one or more letter or number.
         */
    fun checkEmail(email: String): Boolean {
        val withoutDomain = email.split("@")[0]
        if (withoutDomain.length > EMAIL_LOCAL_PART_SIZE) return false
        val char = withoutDomain[withoutDomain.length - 1]
        if (char == '_' || char == '.' || char == '-') return false
        return email.matches(emailRegex.toRegex())
    }

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun createToken(tokenValue: String): Token = tokenEncoder.createValidationInformation(tokenValue)

    fun getTokenExpiration(token: TokenInfo): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createSecurePassword(password: String) = SecurePassword(passwordEncoder.encode(password))
    fun validatePassword(password: String, passwordEncoded: SecurePassword): Boolean =
        passwordEncoder.matches(password, passwordEncoded.encodedPassword)

    fun canBeToken(token: String): Boolean = try {
        Base64.getUrlDecoder()
            .decode(token).size == config.tokenSizeInBytes
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun isTokenTimeValid(
        clock: Clock,
        token: TokenInfo
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
                (now - token.createdAt) <= config.tokenTtl &&
                (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    //TODO repeated function from statistics domain


}
