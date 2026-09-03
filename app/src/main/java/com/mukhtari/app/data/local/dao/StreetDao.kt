package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.StreetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(street: StreetEntity): Long

    @Update
    suspend fun update(street: StreetEntity)

    @Query("SELECT * FROM streets WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: Long): StreetEntity?

    @Query("SELECT * FROM streets WHERE region_id = :regionId AND is_deleted = 0 ORDER BY name ASC")
    fun getStreetsForRegion(regionId: Long): Flow<List<StreetEntity>>

    @Query("UPDATE streets SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)
}
