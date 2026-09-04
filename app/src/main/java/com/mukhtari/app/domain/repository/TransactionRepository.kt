package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    suspend fun getTransactionById(id: Long): TransactionEntity?
    suspend fun saveTransaction(transaction: TransactionEntity): Long
    suspend fun softDeleteTransaction(id: Long)
}
