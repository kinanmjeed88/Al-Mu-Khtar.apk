package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.StreetEntity
import kotlinx.coroutines.flow.Flow

interface StreetRepository {
    suspend fun getStreetById(id: Long): StreetEntity?
    fun getStreetsForRegion(regionId: Long): Flow<List<StreetEntity>>
    suspend fun saveStreet(street: StreetEntity): Long
    suspend fun softDeleteStreet(id: Long)
}
