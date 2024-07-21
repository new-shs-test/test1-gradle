package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.Environment
import com.isel.gomokuApi.domain.model.game.Position
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Tests are not autonomous
 * */
class JdbiRepoTests {

    @BeforeTest
    fun clean(){
        transactionManager.run {
            it.gameRepository.cleanPlays()
        }
    }
    @Test
    fun `store goPieces test`(){
        transactionManager.run {
            it.gameRepository.storePieces(
                1,
                listOf(Position(0,0),Position(0,1), Position(0,2))
            )
        }
    }

    companion object {
        private val transactionManager = JdbiTransactionManager(Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            }
        ).configureWithAppRequirements()
        )
    }
}