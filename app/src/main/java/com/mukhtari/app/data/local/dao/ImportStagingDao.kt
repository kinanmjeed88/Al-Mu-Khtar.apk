package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.ImportStagingEntity

@Dao
interface ImportStagingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(stagingEntities: List<ImportStagingEntity>)

    @Query("SELECT * FROM import_staging WHERE import_session_id = :sessionId")
    suspend fun getStagingBySessionId(sessionId: String): List<ImportStagingEntity>

    @Update
    suspend fun updateStagingRecord(stagingEntity: ImportStagingEntity)

    @Query("DELETE FROM import_staging WHERE import_session_id = :sessionId")
    suspend fun deleteStagingSession(sessionId: String)
}
