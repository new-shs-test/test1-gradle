package com.isel.gomokuApi.repository.model

class UserInfoData (
    val nickname: String,
    val points : Int,
    val wins : Int,
    val loses : Int,
    val draws : Int,
    val totalGames : Int,
    val totalTime : Long
)