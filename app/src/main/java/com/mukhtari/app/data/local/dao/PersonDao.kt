package com.mukhtari.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mukhtari.app.data.local.entity.PersonEntity

@Dao
interface PersonDao {
    @Insert
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Query("SELECT * FROM persons WHERE is_deleted = 0")
    suspend fun getActivePersons(): List<PersonEntity>

    @Query("SELECT * FROM persons WHERE family_id = :familyId AND is_deleted = 0")
    suspend fun getPersonsByFamilyId(familyId: Long): List<PersonEntity>
    
    @Query("SELECT * FROM persons WHERE id = :id AND is_deleted = 0")
    suspend fun getActivePersonById(id: Long): PersonEntity?

    @Query("UPDATE persons SET is_deleted = 1, deleted_at = :deletedAt, deleted_reason = :reason WHERE id = :id")
    suspend fun softDeletePerson(id: Long, deletedAt: Long, reason: String?)
}
