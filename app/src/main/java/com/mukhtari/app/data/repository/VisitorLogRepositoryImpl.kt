package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.VisitorLogDao
import com.mukhtari.app.data.local.entity.VisitorLogEntity
import com.mukhtari.app.domain.repository.VisitorLogRepository
import kotlinx.coroutines.flow.Flow

class VisitorLogRepositoryImpl(
    private val visitorLogDao: VisitorLogDao
) : VisitorLogRepository {
    override fun getAllVisitorLogs(): Flow<List<VisitorLogEntity>> {
        return visitorLogDao.getAllVisitorLogs()
    }

    override suspend fun getVisitorLogById(id: Long): VisitorLogEntity? {
        return visitorLogDao.getById(id)
    }

    override suspend fun saveVisitorLog(visitorLog: VisitorLogEntity): Long {
        return if (visitorLog.id == 0L) {
            visitorLogDao.insert(visitorLog)
        } else {
            visitorLogDao.update(visitorLog)
            visitorLog.id
        }
    }
}
