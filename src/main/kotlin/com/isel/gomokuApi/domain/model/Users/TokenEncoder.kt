package com.isel.gomokuApi.domain.model.Users

import com.isel.gomokuApi.domain.model.Users.Token

interface TokenEncoder {
    fun createValidationInformation(token: String): Token
}