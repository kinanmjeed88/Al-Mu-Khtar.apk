package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.VisitorLogDao
import com.mukhtari.app.data.local.entity.VisitorLogEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.VisitorLogRepository
import kotlinx.coroutines.flow.Flow

class VisitorLogRepositoryImpl(
    private val visitorLogDao: VisitorLogDao,
    private val activityLogRepository: ActivityLogRepository
) : VisitorLogRepository {
    override fun getAllVisitorLogs(): Flow<List<VisitorLogEntity>> {
        return visitorLogDao.getAllVisitorLogs()
    }

    override suspend fun getVisitorLogById(id: Long): VisitorLogEntity? {
        return visitorLogDao.getById(id)
    }

    override suspend fun saveVisitorLog(visitorLog: VisitorLogEntity): Long {
        return if (visitorLog.id == 0L) {
            val id = visitorLogDao.insert(visitorLog)
            activityLogRepository.logActivity("CREATE", "VisitorLog", id, "Created visitor log for \${visitorLog.visitorName}", null, visitorLog.toString())
            id
        } else {
            val old = visitorLogDao.getById(visitorLog.id)
            visitorLogDao.update(visitorLog)
            activityLogRepository.logActivity("UPDATE", "VisitorLog", visitorLog.id, "Updated visitor log for \${visitorLog.visitorName}", old?.toString(), visitorLog.toString())
            visitorLog.id
        }
    }

    override suspend fun deleteVisitorLog(id: Long) {
        val old = visitorLogDao.getById(id)
        visitorLogDao.hardDelete(id)
        activityLogRepository.logActivity("HARD_DELETE", "VisitorLog", id, "Deleted visitor log ID \$id", old?.toString(), null)
    }
}
