package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.VisitorLogEntity
import kotlinx.coroutines.flow.Flow

interface VisitorLogRepository {
    fun getAllVisitorLogs(): Flow<List<VisitorLogEntity>>
    suspend fun getVisitorLogById(id: Long): VisitorLogEntity?
    suspend fun saveVisitorLog(visitorLog: VisitorLogEntity): Long
}
