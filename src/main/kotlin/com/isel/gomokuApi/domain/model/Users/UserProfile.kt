package com.isel.gomokuApi.domain.model.Users

class UserProfile (
                   val id: Int,
                   val nickname: String,
                   val email: String,
                   val points: Int,
                   val victories: Int,
                   val defeats: Int,
                   val draws: Int,
                   val gamesPlayed: Int,
                   val timePlayed: String
)


