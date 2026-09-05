package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.VisitorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitorLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visitorLog: VisitorLogEntity): Long

    @Update
    suspend fun update(visitorLog: VisitorLogEntity)

    @Query("SELECT * FROM visitors_log WHERE id = :id")
    suspend fun getById(id: Long): VisitorLogEntity?

    @Query("SELECT * FROM visitors_log ORDER BY visit_date DESC")
    fun getAllVisitorLogs(): Flow<List<VisitorLogEntity>>

    @Query("DELETE FROM visitors_log WHERE id = :id")
    suspend fun hardDelete(id: Long)
}
