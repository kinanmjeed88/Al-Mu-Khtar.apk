package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long
    
    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY request_date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("UPDATE transactions SET person_id = :newPersonId WHERE person_id = :oldPersonId")
    suspend fun updatePersonId(oldPersonId: Long, newPersonId: Long)

    @Query("SELECT * FROM transactions WHERE id = :id AND is_deleted = 0")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("UPDATE transactions SET is_deleted = 1, deleted_at = :timestamp, deleted_reason = :reason WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long, timestamp: Long, reason: String)

    @Query("SELECT * FROM transactions WHERE is_deleted = 1")
    suspend fun getDeletedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET is_deleted = 0, deleted_at = NULL, deleted_reason = NULL WHERE id = :id")
    suspend fun restoreTransaction(id: Long)

    @Query("DELETE FROM transactions WHERE id = :id AND is_deleted = 1")
    suspend fun hardDeleteTransaction(id: Long)
}
