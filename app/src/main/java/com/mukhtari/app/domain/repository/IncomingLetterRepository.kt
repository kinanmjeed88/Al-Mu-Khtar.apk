package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.IncomingLetterEntity
import kotlinx.coroutines.flow.Flow

interface IncomingLetterRepository {
    fun getAllLetters(): Flow<List<IncomingLetterEntity>>
    suspend fun getLetterById(id: Long): IncomingLetterEntity?
    suspend fun saveLetter(letter: IncomingLetterEntity): Long
    suspend fun softDeleteLetter(id: Long)
}
