package com.mukhtari.app.data.local.dao

import androidx.room.*
import com.mukhtari.app.data.local.entity.ResidencyCertificateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(certificate: ResidencyCertificateEntity): Long

    @Update
    suspend fun update(certificate: ResidencyCertificateEntity)

    @Query("SELECT * FROM residency_certificates WHERE id = :id")
    suspend fun getById(id: Long): ResidencyCertificateEntity?

    @Query("SELECT * FROM residency_certificates WHERE person_id = :personId ORDER BY issued_at DESC")
    fun getCertificatesForPerson(personId: Long): Flow<List<ResidencyCertificateEntity>>
}
