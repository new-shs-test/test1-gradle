package com.isel.gomokuApi.repository.localRep

import com.isel.gomokuApi.repository.Transaction
import com.isel.gomokuApi.repository.TransactionManager

class LocalMemTransactionManager : TransactionManager {
    val transaction = LocalTransaction()
    override fun <R> run(block: (Transaction) -> R): R {
        return block(transaction)
    }
}