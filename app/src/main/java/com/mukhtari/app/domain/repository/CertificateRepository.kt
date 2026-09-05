package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.ResidencyCertificateEntity
import kotlinx.coroutines.flow.Flow

interface CertificateRepository {
    suspend fun saveCertificate(certificate: ResidencyCertificateEntity): Long
    suspend fun getCertificateById(id: Long): ResidencyCertificateEntity?
    fun getCertificatesForPerson(personId: Long): Flow<List<ResidencyCertificateEntity>>
}
