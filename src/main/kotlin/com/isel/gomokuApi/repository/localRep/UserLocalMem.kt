package com.isel.gomokuApi.repository.localRep

import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.model.Users.SecurePassword
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenInfo
import com.isel.gomokuApi.repository.model.UserInfoData
import com.isel.gomokuApi.repository.UserRepository
import com.isel.gomokuApi.repository.localRep.mem.UserDataMem

class UserLocalMem : UserRepository {

    val userMem = UserDataMem()
    override fun getUserInfoById(id: Int): UserInfoData? {
        TODO("Not yet implemented")
    }

    override fun getUserInfoByNickname(nickname: String): UserInfoData? {
        TODO("Not yet implemented")
    }

    override fun getUserByNickname(nickname: String): User? =
        userMem.usersLastState.firstOrNull { it.nickname == nickname }


    override fun getUserByEmail(email: String): User? =
        userMem.usersLastState.firstOrNull { it.email == email }

    override fun storeUser(nickname: String, email: String, password: SecurePassword): Int {
        val index = userMem.usersLastState.size
        userMem.usersLastState.add(index, User(index, nickname, email, password))
        return index
    }

    override fun createToken(tokenInfo: TokenInfo) {
        userMem.tokens[tokenInfo.token] = tokenInfo
    }

    override fun searchForTokenByUserId(id: Int): TokenInfo? {
        var tokenInfo : TokenInfo? = null
        userMem.tokens.forEach { token, info ->
            if (info.userId == id) tokenInfo = info
        }
        return tokenInfo
    }


    override fun getTokenInfoByToken(validatingToken: Token): Pair<User, TokenInfo>? {
        TODO("Not yet implemented")
    }

    override fun updateTokenLastUsed(tokenInfo: TokenInfo, now: Long) {
        TODO("Not yet implemented")
    }

    override fun removeTokenInfoByToken(validatingToken: Token) {
        userMem.tokens.remove(validatingToken)
    }

    override fun deleteUser(userId: Int) {
        TODO("Not yet implemented")
    }
}



