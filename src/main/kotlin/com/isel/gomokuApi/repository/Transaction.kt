package com.isel.gomokuApi.repository

interface Transaction {
    //Add remaining reps

    val statsRepository : StatsRepository

    val userRepository : UserRepository

    val gameRepository : GamesRepository
    fun rollback()
}