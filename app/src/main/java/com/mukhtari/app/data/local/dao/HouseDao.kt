package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.HouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(house: HouseEntity): Long

    @Update
    suspend fun update(house: HouseEntity)

    @Query("SELECT * FROM houses WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: Long): HouseEntity?

    @Query("SELECT * FROM houses WHERE is_deleted = 0 ORDER BY house_number ASC")
    fun getAllHouses(): Flow<List<HouseEntity>>

    @Query("UPDATE houses SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM houses WHERE is_deleted = 1")
    suspend fun getDeletedHouses(): List<HouseEntity>

    @Query("UPDATE houses SET is_deleted = 0 WHERE id = :id")
    suspend fun restoreHouse(id: Long)

    @Query("DELETE FROM houses WHERE id = :id AND is_deleted = 1")
    suspend fun hardDeleteHouse(id: Long)
}
