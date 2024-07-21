package com.isel.gomokuApi.services

import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.UserDomain
import com.isel.gomokuApi.domain.model.Users.*
import com.isel.gomokuApi.repository.TransactionManager
import com.isel.gomokuApi.repository.UserRepository
import com.isel.gomokuApi.services.utils.TimeParser
import com.isel.gomokuApi.utils.Either
import com.isel.gomokuApi.utils.failure
import com.isel.gomokuApi.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component


data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: String,
    val maxAge:Long
)

data class UserExternalValue(val id: Int,val nickname: String, val tokenInfo: TokenExternalInfo)
sealed class UserValidationError {
    object NicknameAlreadyExists : UserValidationError()
    object EmailAlreadyExists : UserValidationError()
    object InvalidPassword : UserValidationError()
    object InvalidEmailFormat : UserValidationError()
    object UnregisteredUser : UserValidationError()
    object IncorrectPassword : UserValidationError()
    object UserAlreadyLogged : UserValidationError()
}

typealias UserOperationResult = Either<UserValidationError, UserExternalValue>

@Component
class UserServices(
    private val userDomain: UserDomain,
    private val transactionManager: TransactionManager,
    private val clock: Clock
) {
    private fun validateInput(password: String, email: String? = null): UserValidationError? {
        if (email != null) {
            if (email.isBlank() || !userDomain.checkEmail(email)) {
                return UserValidationError.InvalidEmailFormat
            }
        }
        if (password.isBlank() || userDomain.checkPassword(password)) {
            return UserValidationError.InvalidPassword
        }
        return null

    }

    private fun createAndStoreToken(
        id: Int,
        userRepo: UserRepository
    ): Pair<String, TokenInfo> {
        val token: String = userDomain.generateTokenValue()
        val now = clock.now()
        val newToken = TokenInfo(
            userDomain.createToken(token),
            id,
            createdAt = now,
            lastUsedAt = now
        )
        userRepo.createToken(newToken)
        return Pair(token, newToken)
    }

    fun register(nickname: String, email: String, password: String): UserOperationResult {
        val inputValidation = validateInput(password, email)
        if (inputValidation != null) {
            return failure(inputValidation)
        }
        val securePassword = userDomain.createSecurePassword(password)
        return transactionManager.run {
            val userRepo = it.userRepository
            if (userRepo.getUserByNickname(nickname) != null) {
                return@run failure(UserValidationError.NicknameAlreadyExists)
            }
            if ( userRepo.getUserByEmail(email) != null) {
                return@run failure(UserValidationError.EmailAlreadyExists)
            }
            val id = userRepo.storeUser(nickname, email, securePassword)
            val (token, newToken) = createAndStoreToken(id, userRepo)
            val tokenExpiration = userDomain.getTokenExpiration(newToken)
            return@run success(UserExternalValue(id,nickname, TokenExternalInfo(token, tokenExpiration.toString(),(tokenExpiration - clock.now()).inWholeSeconds)))
        }
    }
    fun login(nickname: String, password: String): UserOperationResult {
        val inputValidation = validateInput(password)
        if (inputValidation != null) {
            return failure(inputValidation)
        }
        return transactionManager.run {
            val userRepo = it.userRepository
            val user: User =
                userRepo.getUserByNickname(nickname) ?: return@run failure(UserValidationError.UnregisteredUser)
            if (!userDomain.validatePassword(password, user.passwordEncoded)) {
                return@run failure(UserValidationError.IncorrectPassword)
            }
            val oldToken = userRepo.searchForTokenByUserId(user.id)
            if ( oldToken != null) {
                /*if (userDomain.isTokenTimeValid(clock,oldToken)) {
                    Maybe unecessary
                    return@run failure(UserValidationError.UserAlreadyLogged)
                }*/
                userRepo.removeTokenInfoByToken(oldToken.token)
            }
            val (token, newToken) = createAndStoreToken(user.id, userRepo)
            val tokenExpiration = userDomain.getTokenExpiration(newToken)
            success(UserExternalValue(user.id,user.nickname, TokenExternalInfo(token, tokenExpiration.toString(),(tokenExpiration - clock.now()).inWholeSeconds)))
        }
    }

    fun getUserByToken(token: String): User? {
        if (!userDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val userRepo = it.userRepository
            val validatingToken = userDomain.createToken(token)
            val userAndTokenInfo = userRepo.getTokenInfoByToken(validatingToken)
            if (userAndTokenInfo != null) {
                if (userDomain.isTokenTimeValid(clock, userAndTokenInfo.second)){
                    userRepo.updateTokenLastUsed(userAndTokenInfo.second, clock.now().epochSeconds)
                    userAndTokenInfo.first
                }else{
                    userRepo.removeTokenInfoByToken(validatingToken);
                    null
                }
            } else {
                null
            }
        }
    }

    fun logout(token: String): Boolean {
        val validatingToken = userDomain.createToken(token)
        return transactionManager.run {
            val userRepo = it.userRepository
            userRepo.removeTokenInfoByToken(validatingToken)
            true
        }
    }

    fun getUserInfoByName(nickname: String): UserInfo? {
        return transactionManager.run {
            val userRepo = it.userRepository
            val userInfo = userRepo.getUserInfoByNickname(nickname)
            if (userInfo != null) {
                return@run UserInfo(
                    userInfo.nickname,
                    userInfo.points,
                    userInfo.wins,
                    userInfo.loses,
                    userInfo.draws,
                    userInfo.totalGames,
                    TimeParser().convertToTime(userInfo.totalTime).toString()
                )
            }
            null
        }
    }

    fun getUserInfoById (id: Int): UserInfo? {
        return transactionManager.run {
            val userRepo = it.userRepository
            val userInfo =  userRepo.getUserInfoById(id)
            if (userInfo != null) {
                return@run UserInfo(
                    userInfo.nickname,
                    userInfo.points,
                    userInfo.wins,
                    userInfo.loses,
                    userInfo.draws,
                    userInfo.totalGames,
                    TimeParser().convertToTime(userInfo.totalTime).toString()
                )
            }
            null
        }
    }

    fun getUserProfile(token: String): UserProfile? {
        return transactionManager.run {
            val userRepo = it.userRepository
            val user = getUserByToken(token) ?: return@run null
            val userInfo = userRepo.getUserInfoById(user.id) ?: return@run null
            return@run UserProfile(
                user.id,
                userInfo.nickname,
                user.email,
                userInfo.points,
                userInfo.wins,
                userInfo.loses,
                userInfo.draws,
                userInfo.totalGames,
                TimeParser().convertToTime(userInfo.totalTime).toString()
            )
        }
    }
}
