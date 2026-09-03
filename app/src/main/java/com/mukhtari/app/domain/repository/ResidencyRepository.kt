package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.ResidencyEntity

interface ResidencyRepository {
    suspend fun transferPerson(personId: Long, newHouseId: Long, newFamilyId: Long?, newStartDate: String, reason: String?)
    suspend fun getCurrentResidency(personId: Long): ResidencyEntity?
    suspend fun getResidencyHistory(personId: Long): List<ResidencyEntity>
}
