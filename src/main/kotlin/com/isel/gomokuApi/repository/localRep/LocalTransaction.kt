package com.isel.gomokuApi.repository.localRep

import com.isel.gomokuApi.repository.GamesRepository
import com.isel.gomokuApi.repository.StatsRepository
import com.isel.gomokuApi.repository.Transaction
import com.isel.gomokuApi.repository.UserRepository

class LocalTransaction : Transaction {

    override val statsRepository: StatsRepository = StatsLocalMem()
    override val userRepository: UserRepository = UserLocalMem()
    override val gameRepository: GamesRepository = GamesLocalMem()

    override fun rollback() {
    }
}