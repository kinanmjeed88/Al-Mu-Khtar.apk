package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.FamilyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(family: FamilyEntity): Long

    @Update
    suspend fun update(family: FamilyEntity)

    @Query("SELECT * FROM families WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: Long): FamilyEntity?

    @Query("SELECT * FROM families WHERE is_deleted = 0 ORDER BY family_code ASC")
    fun getAllFamilies(): Flow<List<FamilyEntity>>

    @Query("UPDATE families SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM families WHERE is_deleted = 1")
    suspend fun getDeletedFamilies(): List<FamilyEntity>

    @Query("UPDATE families SET is_deleted = 0 WHERE id = :id")
    suspend fun restoreFamily(id: Long)

    @Query("DELETE FROM families WHERE id = :id AND is_deleted = 1")
    suspend fun hardDeleteFamily(id: Long)
}
