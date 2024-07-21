package com.isel.gomokuApi.domain.model.Users

import java.security.MessageDigest
import java.util.*

class Sha256TokenEncoder : TokenEncoder {
    override fun createValidationInformation(token: String): Token =
        Token(hash(token))

    private fun hash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA256")
        return Base64.getUrlEncoder().encodeToString(
            messageDigest.digest(
                Charsets.UTF_8.encode(input).array()
            )
        )
    }
}