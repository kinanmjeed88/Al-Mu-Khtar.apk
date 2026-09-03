package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.ActivityLogDao
import com.mukhtari.app.data.local.entity.ActivityLogEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ActivityLogRepositoryImpl(
    private val activityLogDao: ActivityLogDao
) : ActivityLogRepository {
    override fun getAllActivityLogs(): Flow<List<ActivityLogEntity>> {
        return activityLogDao.getAllActivityLogs()
    }

    override suspend fun logActivity(
        actionType: String,
        entityType: String,
        entityId: Long?,
        description: String,
        oldValues: String?,
        newValues: String?
    ) {
        withContext(Dispatchers.IO) {
            val log = ActivityLogEntity(
                actionType = actionType,
                entityType = entityType,
                entityId = entityId,
                description = description,
                oldValues = oldValues,
                newValues = newValues,
                timestamp = System.currentTimeMillis()
            )
            activityLogDao.insertLog(log)
        }
    }
}
