package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.FamilyEntity
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getAllFamilies(): Flow<List<FamilyEntity>>
    suspend fun getFamilyById(id: Long): FamilyEntity?
    suspend fun saveFamily(family: FamilyEntity): Long
    suspend fun softDeleteFamily(id: Long)
}
