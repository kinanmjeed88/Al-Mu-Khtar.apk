package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.AlleyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlleyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alley: AlleyEntity): Long

    @Update
    suspend fun update(alley: AlleyEntity)

    @Query("SELECT * FROM alleys WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: Long): AlleyEntity?

    @Query("SELECT * FROM alleys WHERE street_id = :streetId AND is_deleted = 0 ORDER BY name ASC")
    fun getAlleysForStreet(streetId: Long): Flow<List<AlleyEntity>>

    @Query("UPDATE alleys SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)
}
