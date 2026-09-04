package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.FamilyDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.FamilyEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.FamilyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FamilyRepositoryImpl(
    private val familyDao: FamilyDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase
) : FamilyRepository {

    override fun getAllFamilies(): Flow<List<FamilyEntity>> {
        return familyDao.getAllFamilies()
    }

    override suspend fun getFamilyById(id: Long): FamilyEntity? = withContext(Dispatchers.IO) {
        familyDao.getById(id)
    }

    override suspend fun saveFamily(family: FamilyEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (family.id == 0L) {
                val id = familyDao.insert(family)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Family",
                    entityId = id,
                    description = "Created family ${family.familyCode}",
                    oldValues = null,
                    newValues = family.toString()
                )
                id
            } else {
                val oldFamily = familyDao.getById(family.id)
                familyDao.update(family)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "Family",
                    entityId = family.id,
                    description = "Updated family ${family.familyCode}",
                    oldValues = oldFamily?.toString(),
                    newValues = family.toString()
                )
                family.id
            }
        }
    }

    override suspend fun softDeleteFamily(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val family = familyDao.getById(id)
            familyDao.softDelete(id)
            activityLogRepository.logActivity(
                actionType = "DELETE",
                entityType = "Family",
                entityId = id,
                description = "Soft deleted family ${family?.familyCode ?: id}",
                oldValues = family?.toString(),
                newValues = null
            )
        }
    }
}
