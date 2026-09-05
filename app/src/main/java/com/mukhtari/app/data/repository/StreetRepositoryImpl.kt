package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.StreetDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.StreetEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.StreetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StreetRepositoryImpl(
    private val streetDao: StreetDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase
) : StreetRepository {

    override suspend fun getStreetById(id: Long): StreetEntity? = withContext(Dispatchers.IO) {
        streetDao.getById(id)
    }

    override fun getStreetsForRegion(regionId: Long): Flow<List<StreetEntity>> {
        return streetDao.getStreetsForRegion(regionId)
    }

    override suspend fun saveStreet(street: StreetEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (street.id == 0L) {
                val id = streetDao.insert(street)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Street",
                    entityId = id,
                    description = "Created street ${street.name}",
                    oldValues = null,
                    newValues = street.toString()
                )
                id
            } else {
                val oldStreet = streetDao.getById(street.id)
                streetDao.update(street)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "Street",
                    entityId = street.id,
                    description = "Updated street ${street.name}",
                    oldValues = oldStreet?.toString(),
                    newValues = street.toString()
                )
                street.id
            }
        }
    }

    override suspend fun softDeleteStreet(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val street = streetDao.getById(id)
            streetDao.softDelete(id)
            activityLogRepository.logActivity(
                actionType = "DELETE",
                entityType = "Street",
                entityId = id,
                description = "Soft deleted street ${street?.name ?: id}",
                oldValues = street?.toString(),
                newValues = null
            )
        }
    }
}
