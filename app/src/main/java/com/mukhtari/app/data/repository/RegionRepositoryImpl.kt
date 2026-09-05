package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.RegionDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.RegionEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.RegionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegionRepositoryImpl(
    private val regionDao: RegionDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase
) : RegionRepository {
    override suspend fun getActiveRegions(): List<RegionEntity> = withContext(Dispatchers.IO) {
        regionDao.getActiveRegions()
    }

    override suspend fun getActiveRegionById(id: Long): RegionEntity? = withContext(Dispatchers.IO) {
        regionDao.getActiveRegionById(id)
    }

    override suspend fun saveRegion(region: RegionEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (region.id == 0L) {
                val id = regionDao.insertRegion(region)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Region",
                    entityId = id,
                    description = "Created region ${region.name}",
                    oldValues = null,
                    newValues = region.toString()
                )
                id
            } else {
                val oldRegion = regionDao.getActiveRegionById(region.id)
                regionDao.updateRegion(region)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "Region",
                    entityId = region.id,
                    description = "Updated region ${region.name}",
                    oldValues = oldRegion?.toString(),
                    newValues = region.toString()
                )
                region.id
            }
        }
    }

    override suspend fun softDeleteRegion(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val region = regionDao.getActiveRegionById(id)
            regionDao.softDeleteRegion(id, System.currentTimeMillis(), "Deleted by user")
            activityLogRepository.logActivity(
                actionType = "DELETE",
                entityType = "Region",
                entityId = id,
                description = "Soft deleted region ${region?.name ?: id}",
                oldValues = region?.toString(),
                newValues = null
            )
        }
    }

    override suspend fun getDeletedRegions(): List<RegionEntity> = withContext(Dispatchers.IO) {
        regionDao.getDeletedRegions()
    }

    override suspend fun restoreRegion(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            regionDao.restoreRegion(id)
            val region = regionDao.getActiveRegionById(id)
            activityLogRepository.logActivity(
                actionType = "RESTORE",
                entityType = "Region",
                entityId = id,
                description = "Restored region ${region?.name ?: id}",
                oldValues = null,
                newValues = region?.toString()
            )
        }
    }

    override suspend fun hardDeleteRegion(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            regionDao.hardDeleteRegion(id)
            activityLogRepository.logActivity(
                actionType = "HARD_DELETE",
                entityType = "Region",
                entityId = id,
                description = "Hard deleted region ID $id",
                oldValues = null,
                newValues = null
            )
        }
    }
}
