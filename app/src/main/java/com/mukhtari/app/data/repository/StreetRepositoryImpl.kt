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
                val finalStreet = if (street.publicCode.isEmpty()) {
                    street.copy(publicCode = "STR-" + java.util.UUID.randomUUID().toString().take(8).uppercase())
                } else {
                    street
                }
                val id = streetDao.insert(finalStreet)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Street",
                    entityId = id,
                    description = "Created street ${finalStreet.name}",
                    oldValues = null,
                    newValues = finalStreet.toString()
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

    override suspend fun getDeletedStreets(): List<StreetEntity> = withContext(Dispatchers.IO) {
        streetDao.getDeletedStreets()
    }

    override suspend fun restoreStreet(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            streetDao.restoreStreet(id)
            val street = streetDao.getById(id)
            activityLogRepository.logActivity(
                actionType = "RESTORE",
                entityType = "Street",
                entityId = id,
                description = "Restored street ${street?.name ?: id}",
                oldValues = null,
                newValues = street?.toString()
            )
        }
    }

    override suspend fun hardDeleteStreet(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            streetDao.hardDeleteStreet(id)
            activityLogRepository.logActivity(
                actionType = "HARD_DELETE",
                entityType = "Street",
                entityId = id,
                description = "Hard deleted street ID $id",
                oldValues = null,
                newValues = null
            )
        }
    }
}
