package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.RegionEntity

interface RegionRepository {
    suspend fun getActiveRegions(): List<RegionEntity>
    suspend fun getActiveRegionById(id: Long): RegionEntity?
    suspend fun saveRegion(region: RegionEntity): Long
    suspend fun softDeleteRegion(id: Long)
    suspend fun getDeletedRegions(): List<RegionEntity>
    suspend fun restoreRegion(id: Long)
    suspend fun hardDeleteRegion(id: Long)
}
