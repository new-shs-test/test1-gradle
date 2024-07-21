package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.repository.Transaction
import com.isel.gomokuApi.repository.TransactionManager
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class JdbiTransactionManager(private val jdbi : Jdbi) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R,Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
