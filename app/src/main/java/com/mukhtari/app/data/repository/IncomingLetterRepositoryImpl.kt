package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.IncomingLetterDao
import com.mukhtari.app.data.local.entity.IncomingLetterEntity
import com.mukhtari.app.domain.repository.IncomingLetterRepository
import kotlinx.coroutines.flow.Flow

class IncomingLetterRepositoryImpl(
    private val incomingLetterDao: IncomingLetterDao
) : IncomingLetterRepository {
    override fun getAllLetters(): Flow<List<IncomingLetterEntity>> {
        return incomingLetterDao.getAllLetters()
    }

    override suspend fun getLetterById(id: Long): IncomingLetterEntity? {
        return incomingLetterDao.getById(id)
    }

    override suspend fun saveLetter(letter: IncomingLetterEntity): Long {
        return if (letter.id == 0L) {
            incomingLetterDao.insert(letter)
        } else {
            incomingLetterDao.update(letter)
            letter.id
        }
    }

    override suspend fun softDeleteLetter(id: Long) {
        incomingLetterDao.softDelete(id)
    }
}
