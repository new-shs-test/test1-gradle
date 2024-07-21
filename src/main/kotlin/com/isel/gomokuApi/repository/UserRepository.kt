package com.isel.gomokuApi.repository

import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenInfo
import com.isel.gomokuApi.repository.model.UserInfoData

interface UserRepository {
    fun getUserInfoById(id: Int): UserInfoData?
    fun getUserInfoByNickname(nickname: String): UserInfoData?
    fun getUserByNickname(nickname: String): User?
    fun getUserByEmail(email: String): User?
    fun storeUser(nickname: String, email: String, password: SecurePassword) : Int
    fun createToken(tokenInfo: TokenInfo)
    fun searchForTokenByUserId(id: Int): TokenInfo?
    fun getTokenInfoByToken(validatingToken: Token): Pair<User, TokenInfo>?
    fun updateTokenLastUsed(tokenInfo: TokenInfo, now: Long)
    fun removeTokenInfoByToken(validatingToken: Token)
    fun deleteUser(userId: Int)

}
