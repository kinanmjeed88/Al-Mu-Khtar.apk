package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity): Long
    
    @Update
    suspend fun update(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE owner_type = :ownerType AND owner_id = :ownerId AND is_deleted = 0")
    fun getAttachmentsForOwner(ownerType: String, ownerId: Long): Flow<List<AttachmentEntity>>
    
    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: Long): AttachmentEntity?

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("SELECT COUNT(*) FROM attachments WHERE file_path = :filePath AND is_deleted = 0 AND id != :excludeId")
    suspend fun getActiveReferencesCount(filePath: String, excludeId: Long): Int

    @Query("UPDATE attachments SET owner_id = :newOwnerId WHERE owner_type = 'person' AND owner_id = :oldOwnerId")
    suspend fun updateOwnerIdForPerson(oldOwnerId: Long, newOwnerId: Long)
}
