package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.OutgoingLetterDao
import com.mukhtari.app.data.local.entity.OutgoingLetterEntity
import com.mukhtari.app.domain.repository.OutgoingLetterRepository
import kotlinx.coroutines.flow.Flow

class OutgoingLetterRepositoryImpl(
    private val outgoingLetterDao: OutgoingLetterDao
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
            outgoingLetterDao.insert(finalLetter)
        } else {
            outgoingLetterDao.update(letter)
            letter.id
        }
    }

    override suspend fun softDeleteLetter(id: Long) {
        outgoingLetterDao.softDelete(id)
    }

    override suspend fun getDeletedLetters(): List<OutgoingLetterEntity> {
        return outgoingLetterDao.getDeletedLetters()
    }

    override suspend fun restoreLetter(id: Long) {
        outgoingLetterDao.restoreLetter(id)
    }

    override suspend fun hardDeleteLetter(id: Long) {
        outgoingLetterDao.hardDeleteLetter(id)
    }
}
