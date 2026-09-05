package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.CertificateDao
import com.mukhtari.app.data.local.entity.ResidencyCertificateEntity
import com.mukhtari.app.domain.repository.CertificateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CertificateRepositoryImpl(
    private val certificateDao: CertificateDao
) : CertificateRepository {
    override suspend fun saveCertificate(certificate: ResidencyCertificateEntity): Long = withContext(Dispatchers.IO) {
        if (certificate.id == 0L) {
            certificateDao.insert(certificate)
        } else {
            certificateDao.update(certificate)
            certificate.id
        }
    }

    override suspend fun getCertificateById(id: Long): ResidencyCertificateEntity? = withContext(Dispatchers.IO) {
        certificateDao.getById(id)
    }

    override fun getCertificatesForPerson(personId: Long): Flow<List<ResidencyCertificateEntity>> {
        return certificateDao.getCertificatesForPerson(personId)
    }
}
