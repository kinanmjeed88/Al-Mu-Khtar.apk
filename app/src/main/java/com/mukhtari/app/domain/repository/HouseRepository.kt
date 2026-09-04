package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.HouseEntity
import kotlinx.coroutines.flow.Flow

interface HouseRepository {
    fun getAllHouses(): Flow<List<HouseEntity>>
    suspend fun getHouseById(id: Long): HouseEntity?
    suspend fun saveHouse(house: HouseEntity): Long
    suspend fun softDeleteHouse(id: Long)
}
