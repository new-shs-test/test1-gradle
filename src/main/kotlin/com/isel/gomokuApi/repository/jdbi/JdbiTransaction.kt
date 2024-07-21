package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.repository.GamesRepository
import com.isel.gomokuApi.repository.StatsRepository
import com.isel.gomokuApi.repository.Transaction
import com.isel.gomokuApi.repository.UserRepository
import org.jdbi.v3.core.Handle

class JdbiTransaction(private val handle : Handle) : Transaction {

    override val statsRepository: StatsRepository = JdbiStatsRepository(handle)
    override val gameRepository: GamesRepository = JdbiGamesRepository(handle)
    override val userRepository: UserRepository = JdbiUserRepository(handle)


    override fun rollback() {
        handle.rollback()
    }
}