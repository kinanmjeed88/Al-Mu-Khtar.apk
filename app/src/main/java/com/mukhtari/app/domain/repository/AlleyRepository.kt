package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.AlleyEntity
import kotlinx.coroutines.flow.Flow

interface AlleyRepository {
    suspend fun getAlleyById(id: Long): AlleyEntity?
    fun getAlleysForStreet(streetId: Long): Flow<List<AlleyEntity>>
    suspend fun saveAlley(alley: AlleyEntity): Long
    suspend fun softDeleteAlley(id: Long)
}
