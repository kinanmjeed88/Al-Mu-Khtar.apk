package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.OutgoingLetterEntity
import kotlinx.coroutines.flow.Flow

interface OutgoingLetterRepository {
    fun getAllLetters(): Flow<List<OutgoingLetterEntity>>
    suspend fun getLetterById(id: Long): OutgoingLetterEntity?
    suspend fun saveLetter(letter: OutgoingLetterEntity): Long
    suspend fun softDeleteLetter(id: Long)
    suspend fun getDeletedLetters(): List<OutgoingLetterEntity>
    suspend fun restoreLetter(id: Long)
    suspend fun hardDeleteLetter(id: Long)
}
