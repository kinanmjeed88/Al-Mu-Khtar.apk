package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.PersonDao
import com.mukhtari.app.data.local.dao.ResidencyDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.ResidencyEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.ResidencyRepository

class ResidencyRepositoryImpl(
    private val db: AppDatabase,
    private val residencyDao: ResidencyDao,
    private val personDao: PersonDao,
    private val activityLogRepository: ActivityLogRepository
) : ResidencyRepository {

    override suspend fun transferPerson(
        personId: Long,
        newHouseId: Long,
        newFamilyId: Long?,
        newStartDate: String,
        reason: String?
    ) {
        db.withTransaction {
            // 1. Close current residency
            val currentResidency = residencyDao.getCurrentResidencyForPerson(personId)
            if (currentResidency != null) {
                if (newStartDate < currentResidency.startDate) {
                    throw IllegalArgumentException("New start date cannot be before current residency start date")
                }
                residencyDao.updateResidency(
                    currentResidency.copy(
                        endDate = newStartDate,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            // 2. Create new residency
            val newResidency = ResidencyEntity(
                personId = personId,
                familyId = newFamilyId,
                houseId = newHouseId,
                startDate = newStartDate,
                endDate = null,
                residencyType = "resident",
                verificationStatus = "verified",
                reason = reason,
                previousAddressText = null,
                notes = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            residencyDao.insertResidency(newResidency)

            // 3. Update Person's house and family references
            val person = personDao.getActivePersonById(personId)
            if (person != null) {
                val newPerson = person.copy(
                    houseId = newHouseId,
                    familyId = newFamilyId,
                    updatedAt = System.currentTimeMillis()
                )
                personDao.updatePerson(newPerson)

                activityLogRepository.logActivity(
                    actionType = "TRANSFER",
                    entityType = "Residency",
                    entityId = personId,
                    description = "Transferred person ${person.fullName} to house $newHouseId",
                    oldValues = person.toString(),
                    newValues = newPerson.toString()
                )
            }
        }
    }

    override suspend fun getCurrentResidency(personId: Long): ResidencyEntity? {
        return residencyDao.getCurrentResidencyForPerson(personId)
    }

    override suspend fun getResidencyHistory(personId: Long): List<ResidencyEntity> {
        return residencyDao.getResidenciesForPerson(personId)
    }
}
