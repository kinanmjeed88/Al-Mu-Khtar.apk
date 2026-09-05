package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.TransactionDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.TransactionEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun getTransactionById(id: Long): TransactionEntity? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }

    override suspend fun saveTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (transaction.id == 0L) {
                val id = transactionDao.insert(transaction)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Transaction",
                    entityId = id,
                    description = "Created transaction ${transaction.transactionCode}",
                    oldValues = null,
                    newValues = transaction.toString()
                )
                id
            } else {
                val oldTransaction = transactionDao.getTransactionById(transaction.id)
                transactionDao.update(transaction)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "Transaction",
                    entityId = transaction.id,
                    description = "Updated transaction ${transaction.transactionCode}",
                    oldValues = oldTransaction?.toString(),
                    newValues = transaction.toString()
                )
                transaction.id
            }
        }
    }

    override suspend fun softDeleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val transaction = transactionDao.getTransactionById(id)
            if (transaction != null) {
                transactionDao.softDeleteTransaction(id, System.currentTimeMillis(), "Deleted by user")
                activityLogRepository.logActivity(
                    actionType = "DELETE",
                    entityType = "Transaction",
                    entityId = id,
                    description = "Soft deleted transaction ${transaction.transactionCode}",
                    oldValues = transaction.toString(),
                    newValues = null
                )
            }
        }
    }
override suspend fun getDeletedTransactions(): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.getDeletedTransactions()
    }

    override suspend fun restoreTransaction(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            transactionDao.restoreTransaction(id)
            val transaction = transactionDao.getTransactionById(id)
            if (transaction != null) {
                activityLogRepository.logActivity(
                    actionType = "RESTORE",
                    entityType = "Transaction",
                    entityId = id,
                    description = "Restored transaction ${transaction.transactionCode}",
                    oldValues = null,
                    newValues = transaction.toString()
                )
            }
        }
    }

    override suspend fun hardDeleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            transactionDao.hardDeleteTransaction(id)
            activityLogRepository.logActivity(
                actionType = "HARD_DELETE",
                entityType = "Transaction",
                entityId = id,
                description = "Hard deleted transaction ID $id",
                oldValues = null,
                newValues = null
            )
        }
    }
}