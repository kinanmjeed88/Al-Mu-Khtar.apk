package com.mukhtari.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mukhtari.app.data.local.entity.RegionEntity

@Dao
interface RegionDao {
    @Insert
    suspend fun insertRegion(region: RegionEntity): Long

    @Update
    suspend fun updateRegion(region: RegionEntity)

    @Query("SELECT * FROM regions WHERE is_deleted = 0")
    suspend fun getActiveRegions(): List<RegionEntity>

    @Query("SELECT * FROM regions WHERE is_deleted = 1")
    suspend fun getDeletedRegions(): List<RegionEntity>

    @Query("SELECT * FROM regions WHERE id = :id AND is_deleted = 0")
    suspend fun getActiveRegionById(id: Long): RegionEntity?

    @Query("UPDATE regions SET is_deleted = 1, deleted_at = :deletedAt, deleted_reason = :reason WHERE id = :id")
    suspend fun softDeleteRegion(id: Long, deletedAt: Long, reason: String?)

    @Query("UPDATE regions SET is_deleted = 0, deleted_at = NULL, deleted_reason = NULL WHERE id = :id")
    suspend fun restoreRegion(id: Long)

    @Query("DELETE FROM regions WHERE id = :id AND is_deleted = 1")
    suspend fun hardDeleteRegion(id: Long)
}
