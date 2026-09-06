package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.OutgoingLetterDao
import com.mukhtari.app.data.local.entity.OutgoingLetterEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import com.mukhtari.app.domain.repository.OutgoingLetterRepository
import kotlinx.coroutines.flow.Flow

class OutgoingLetterRepositoryImpl(
    private val outgoingLetterDao: OutgoingLetterDao,
    private val activityLogRepository: ActivityLogRepository
) : OutgoingLetterRepository {
    override fun getAllLetters(): Flow<List<OutgoingLetterEntity>> {
        return outgoingLetterDao.getAllLetters()
    }

    override suspend fun getLetterById(id: Long): OutgoingLetterEntity? {
        return outgoingLetterDao.getById(id)
    }

    override suspend fun saveLetter(letter: OutgoingLetterEntity): Long {
        return if (letter.id == 0L) {
            val finalLetter = if (letter.publicCode.isEmpty()) {
                letter.copy(publicCode = java.util.UUID.randomUUID().toString().take(8).uppercase())
            } else {
                letter
            }
            val id = outgoingLetterDao.insert(finalLetter)
            activityLogRepository.logActivity("CREATE", "OutgoingLetter", id, "Created outgoing letter \${finalLetter.subject}", null, finalLetter.toString())
            id
        } else {
            val old = outgoingLetterDao.getById(letter.id)
            outgoingLetterDao.update(letter)
            activityLogRepository.logActivity("UPDATE", "OutgoingLetter", letter.id, "Updated outgoing letter \${letter.subject}", old?.toString(), letter.toString())
            letter.id
        }
    }

    override suspend fun softDeleteLetter(id: Long) {
        val old = outgoingLetterDao.getById(id)
        outgoingLetterDao.softDelete(id)
        activityLogRepository.logActivity("DELETE", "OutgoingLetter", id, "Soft deleted outgoing letter ID \$id", old?.toString(), null)
    }

    override suspend fun getDeletedLetters(): List<OutgoingLetterEntity> {
        return outgoingLetterDao.getDeletedLetters()
    }

    override suspend fun restoreLetter(id: Long) {
        outgoingLetterDao.restoreLetter(id)
        val restored = outgoingLetterDao.getById(id)
        activityLogRepository.logActivity("RESTORE", "OutgoingLetter", id, "Restored outgoing letter ID \$id", null, restored?.toString())
    }

    override suspend fun hardDeleteLetter(id: Long) {
        outgoingLetterDao.hardDeleteLetter(id)
        activityLogRepository.logActivity("HARD_DELETE", "OutgoingLetter", id, "Hard deleted outgoing letter ID \$id", null, null)
    }
}
