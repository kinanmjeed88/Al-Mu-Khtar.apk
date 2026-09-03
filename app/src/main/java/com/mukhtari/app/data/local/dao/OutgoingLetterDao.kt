package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.OutgoingLetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutgoingLetterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(letter: OutgoingLetterEntity): Long

    @Update
    suspend fun update(letter: OutgoingLetterEntity)

    @Query("SELECT * FROM outgoing_letters WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: Long): OutgoingLetterEntity?

    @Query("SELECT * FROM outgoing_letters WHERE is_deleted = 0 ORDER BY letter_date DESC")
    fun getAllLetters(): Flow<List<OutgoingLetterEntity>>

    @Query("UPDATE outgoing_letters SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)
}
