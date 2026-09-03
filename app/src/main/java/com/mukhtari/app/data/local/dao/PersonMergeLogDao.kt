package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.PersonMergeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonMergeLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PersonMergeLogEntity): Long

    @Query("SELECT * FROM person_merge_log ORDER BY merged_at DESC")
    fun getAllMergeLogs(): Flow<List<PersonMergeLogEntity>>
}
