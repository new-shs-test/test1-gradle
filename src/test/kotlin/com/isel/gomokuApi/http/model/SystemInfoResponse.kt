package com.isel.gomokuApi.http.model

data class StatsResponse(
    val authors: List<Author>
)
data class Author(
    val name: String,
    val email: String
)