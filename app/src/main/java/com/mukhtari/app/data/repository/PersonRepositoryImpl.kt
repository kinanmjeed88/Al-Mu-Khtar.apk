package com.mukhtari.app.data.repository

import androidx.room.withTransaction
import com.mukhtari.app.data.local.dao.PersonDao
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.PersonEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.PersonRepository
import com.mukhtari.app.domain.usecase.ArabicNormalizationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonRepositoryImpl(
    private val personDao: PersonDao,
    private val activityLogRepository: ActivityLogRepository,
    private val db: AppDatabase,
    private val normalizationUseCase: ArabicNormalizationUseCase
) : PersonRepository {

    override suspend fun getActivePersons(): List<PersonEntity> = withContext(Dispatchers.IO) {
        personDao.getActivePersons()
    }

    override suspend fun getActivePersonById(id: Long): PersonEntity? = withContext(Dispatchers.IO) {
        personDao.getActivePersonById(id)
    }

    override suspend fun savePerson(person: PersonEntity): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (person.id == 0L) {
                val id = personDao.insertPerson(person)
                activityLogRepository.logActivity(
                    actionType = "CREATE",
                    entityType = "Person",
                    entityId = id,
                    description = "Created person ${person.fullName}",
                    oldValues = null,
                    newValues = person.toString()
                )
                id
            } else {
                val oldPerson = personDao.getActivePersonById(person.id)
                personDao.updatePerson(person)
                activityLogRepository.logActivity(
                    actionType = "UPDATE",
                    entityType = "Person",
                    entityId = person.id,
                    description = "Updated person ${person.fullName}",
                    oldValues = oldPerson?.toString(),
                    newValues = person.toString()
                )
                person.id
            }
        }
    }

    override suspend fun softDeletePerson(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val person = personDao.getActivePersonById(id)
            personDao.softDeletePerson(id, System.currentTimeMillis(), "Deleted by user")
            activityLogRepository.logActivity(
                actionType = "DELETE",
                entityType = "Person",
                entityId = id,
                description = "Soft deleted person ${person?.fullName ?: id}",
                oldValues = person?.toString(),
                newValues = null
            )
        }
    }

    override suspend fun searchPersons(query: String): List<PersonEntity> = withContext(Dispatchers.IO) {
        val persons = personDao.getActivePersons()
        if (query.isBlank()) {
            persons
        } else {
            val normalizedQuery = normalizationUseCase(query)
            persons.filter {
                val normalizedName = normalizationUseCase(it.fullName)
                normalizedName.contains(normalizedQuery)
            }
        }
    }
}
