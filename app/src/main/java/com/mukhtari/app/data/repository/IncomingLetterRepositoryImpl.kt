package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.IncomingLetterDao
import com.mukhtari.app.data.local.entity.IncomingLetterEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.IncomingLetterRepository
import kotlinx.coroutines.flow.Flow

class IncomingLetterRepositoryImpl(
    private val incomingLetterDao: IncomingLetterDao,
    private val activityLogRepository: ActivityLogRepository
) : IncomingLetterRepository {
    override fun getAllLetters(): Flow<List<IncomingLetterEntity>> {
        return incomingLetterDao.getAllLetters()
    }

    override suspend fun getLetterById(id: Long): IncomingLetterEntity? {
        return incomingLetterDao.getById(id)
    }

    override suspend fun saveLetter(letter: IncomingLetterEntity): Long {
        return if (letter.id == 0L) {
            val finalLetter = if (letter.publicCode.isEmpty()) {
                letter.copy(publicCode = java.util.UUID.randomUUID().toString().take(8).uppercase())
            } else {
                letter
            }
            val id = incomingLetterDao.insert(finalLetter)
            activityLogRepository.logActivity("CREATE", "IncomingLetter", id, "Created incoming letter \${finalLetter.subject}", null, finalLetter.toString())
            id
        } else {
            val old = incomingLetterDao.getById(letter.id)
            incomingLetterDao.update(letter)
            activityLogRepository.logActivity("UPDATE", "IncomingLetter", letter.id, "Updated incoming letter \${letter.subject}", old?.toString(), letter.toString())
            letter.id
        }
    }

    override suspend fun softDeleteLetter(id: Long) {
        val old = incomingLetterDao.getById(id)
        incomingLetterDao.softDelete(id)
        activityLogRepository.logActivity("DELETE", "IncomingLetter", id, "Soft deleted incoming letter ID \$id", old?.toString(), null)
    }

    override suspend fun getDeletedLetters(): List<IncomingLetterEntity> {
        return incomingLetterDao.getDeletedLetters()
    }

    override suspend fun restoreLetter(id: Long) {
        incomingLetterDao.restoreLetter(id)
        val restored = incomingLetterDao.getById(id)
        activityLogRepository.logActivity("RESTORE", "IncomingLetter", id, "Restored incoming letter ID \$id", null, restored?.toString())
    }

    override suspend fun hardDeleteLetter(id: Long) {
        incomingLetterDao.hardDeleteLetter(id)
        activityLogRepository.logActivity("HARD_DELETE", "IncomingLetter", id, "Hard deleted incoming letter ID \$id", null, null)
    }
}
