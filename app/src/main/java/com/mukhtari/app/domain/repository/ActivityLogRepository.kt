package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {
    fun getAllActivityLogs(): Flow<List<ActivityLogEntity>>
    suspend fun logActivity(
        actionType: String,
        entityType: String,
        entityId: Long?,
        description: String,
        oldValues: String?,
        newValues: String?
    )
}
