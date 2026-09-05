package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.IncomingLetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomingLetterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(letter: IncomingLetterEntity): Long

    @Update
    suspend fun update(letter: IncomingLetterEntity)

    @Query("SELECT * FROM incoming_letters WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: Long): IncomingLetterEntity?

    @Query("SELECT * FROM incoming_letters WHERE is_deleted = 0 ORDER BY letter_date DESC")
    fun getAllLetters(): Flow<List<IncomingLetterEntity>>

    @Query("UPDATE incoming_letters SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM incoming_letters WHERE is_deleted = 1")
    suspend fun getDeletedLetters(): List<IncomingLetterEntity>

    @Query("UPDATE incoming_letters SET is_deleted = 0 WHERE id = :id")
    suspend fun restoreLetter(id: Long)

    @Query("DELETE FROM incoming_letters WHERE id = :id AND is_deleted = 1")
    suspend fun hardDeleteLetter(id: Long)
}
