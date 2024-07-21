package com.isel.gomokuApi.http.model

import org.springframework.web.client.HttpClientErrorException.UnprocessableEntity

object StatusCode {
    const val OK = 200
    const val CREATED = 201
    const val BAD_REQUEST = 400
    const val NOT_FOUND = 404
    const val FORBIDDEN = 403
    const val INTERNAL_SERVER_ERROR = 500

}