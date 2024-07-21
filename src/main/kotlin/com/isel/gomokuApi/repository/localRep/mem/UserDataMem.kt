package com.isel.gomokuApi.repository.localRep.mem

import com.isel.gomokuApi.domain.User
import com.isel.gomokuApi.domain.model.Users.Token
import com.isel.gomokuApi.domain.model.Users.TokenInfo

class UserDataMem {
    val usersLastState = mutableListOf<User>()

    //Key represents userId's
    val tokens = HashMap<Token, TokenInfo>()
}