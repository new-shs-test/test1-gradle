package com.isel.gomokuApi.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
