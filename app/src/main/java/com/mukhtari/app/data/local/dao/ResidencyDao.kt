package com.mukhtari.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mukhtari.app.data.local.entity.ResidencyEntity

@Dao
interface ResidencyDao {
    @Insert
    suspend fun insertResidency(residency: ResidencyEntity): Long

    @Update
    suspend fun updateResidency(residency: ResidencyEntity)

    @Query("SELECT * FROM residencies WHERE person_id = :personId")
    suspend fun getResidenciesForPerson(personId: Long): List<ResidencyEntity>

    @Query("SELECT * FROM residencies WHERE person_id = :personId AND (end_date IS NULL OR end_date = '')")
    suspend fun getCurrentResidencyForPerson(personId: Long): ResidencyEntity?
}
