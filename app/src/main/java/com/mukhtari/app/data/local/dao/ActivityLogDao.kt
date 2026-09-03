package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC")
    fun getAllActivityLogs(): Flow<List<ActivityLogEntity>>
}
