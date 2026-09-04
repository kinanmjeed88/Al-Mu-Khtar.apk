package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.HouseDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.HouseEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.HouseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HouseRepositoryImpl(
    private val houseDao: HouseDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase
) : HouseRepository {

    override fun getAllHouses(): Flow<List<HouseEntity>> {
        return houseDao.getAllHouses()
    }

    override suspend fun getHouseById(id: Long): HouseEntity? = withContext(Dispatchers.IO) {
        houseDao.getById(id)
    }

    override suspend fun saveHouse(house: HouseEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (house.id == 0L) {
                val id = houseDao.insert(house)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "House",
                    entityId = id,
                    description = "Created house ${house.houseNumber}",
                    oldValues = null,
                    newValues = house.toString()
                )
                id
            } else {
                val oldHouse = houseDao.getById(house.id)
                houseDao.update(house)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "House",
                    entityId = house.id,
                    description = "Updated house ${house.houseNumber}",
                    oldValues = oldHouse?.toString(),
                    newValues = house.toString()
                )
                house.id
            }
        }
    }

    override suspend fun softDeleteHouse(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val house = houseDao.getById(id)
            houseDao.softDelete(id)
            activityLogRepository.logActivity(
                actionType = "DELETE",
                entityType = "House",
                entityId = id,
                description = "Soft deleted house ${house?.houseNumber ?: id}",
                oldValues = house?.toString(),
                newValues = null
            )
        }
    }
}
