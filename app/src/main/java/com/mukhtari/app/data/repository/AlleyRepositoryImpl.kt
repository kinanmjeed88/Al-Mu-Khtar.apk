package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.AlleyDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.AlleyEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.AlleyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AlleyRepositoryImpl(
    private val alleyDao: AlleyDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase
) : AlleyRepository {

    override suspend fun getAlleyById(id: Long): AlleyEntity? = withContext(Dispatchers.IO) {
        alleyDao.getById(id)
    }

    override fun getAlleysForStreet(streetId: Long): Flow<List<AlleyEntity>> {
        return alleyDao.getAlleysForStreet(streetId)
    }

    override suspend fun saveAlley(alley: AlleyEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (alley.id == 0L) {
                val id = alleyDao.insert(alley)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Alley",
                    entityId = id,
                    description = "Created alley ${alley.name}",
                    oldValues = null,
                    newValues = alley.toString()
                )
                id
            } else {
                val oldAlley = alleyDao.getById(alley.id)
                alleyDao.update(alley)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "Alley",
                    entityId = alley.id,
                    description = "Updated alley ${alley.name}",
                    oldValues = oldAlley?.toString(),
                    newValues = alley.toString()
                )
                alley.id
            }
        }
    }

    override suspend fun softDeleteAlley(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val alley = alleyDao.getById(id)
            alleyDao.softDelete(id)
            activityLogRepository.logActivity(
                actionType = "DELETE",
                entityType = "Alley",
                entityId = id,
                description = "Soft deleted alley ${alley?.name ?: id}",
                oldValues = alley?.toString(),
                newValues = null
            )
        }
    }
}
