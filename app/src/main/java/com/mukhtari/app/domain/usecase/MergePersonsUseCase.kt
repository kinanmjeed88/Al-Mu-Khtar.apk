package com.mukhtari.app.domain.usecase

import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.PersonMergeLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MergePersonsUseCase(
    private val db: AppDatabase
) {
    suspend operator fun invoke(sourcePersonId: Long, targetPersonId: Long, reason: String?): Boolean = withContext(Dispatchers.IO) {
        if (sourcePersonId == targetPersonId) return@withContext false

        try {
            db.withTransaction {
                val sourcePerson = db.personDao().getActivePersonById(sourcePersonId)
                val targetPerson = db.personDao().getActivePersonById(targetPersonId)

                if (sourcePerson == null || targetPerson == null) {
                    throw IllegalStateException("One or both persons not found or deleted")
                }

                // 1. Transfer Transactions
                db.transactionDao().updatePersonId(sourcePersonId, targetPersonId)

                // 2. Transfer Residencies
                db.query(SimpleSQLiteQuery("UPDATE residencies SET person_id = $targetPersonId WHERE person_id = $sourcePersonId")).moveToFirst()

                // 3. Transfer Certificates
                db.query(SimpleSQLiteQuery("UPDATE residency_certificates SET person_id = $targetPersonId WHERE person_id = $sourcePersonId")).moveToFirst()

                // 4. Transfer Attachments
                db.attachmentDao().updateOwnerIdForPerson(sourcePersonId, targetPersonId)

                // 5. Soft Delete Source Person
                db.personDao().softDeletePerson(sourcePersonId, System.currentTimeMillis(), "Merged into person $targetPersonId: $reason")

                // 6. Log Merge
                db.personMergeLogDao().insertLog(
                    PersonMergeLogEntity(
                        sourcePersonId = sourcePersonId,
                        targetPersonId = targetPersonId,
                        mergedAt = System.currentTimeMillis(),
                        reason = reason,
                        details = "Source ID: $sourcePersonId merged into Target ID: $targetPersonId"
                    )
                )

                // 7. Activity Log
                db.activityLogDao().insertLog(
                    com.mukhtari.app.data.local.entity.ActivityLogEntity(
                        actionType = "MERGE",
                        entityType = "Person",
                        entityId = targetPersonId,
                        description = "Merged person ${sourcePerson.fullName} (ID: $sourcePersonId) into ${targetPerson.fullName} (ID: $targetPersonId)",
                        oldValues = sourcePerson.toString(),
                        newValues = targetPerson.toString(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
