package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.RegionDao
import com.mukhtari.app.data.local.entity.RegionEntity
import com.mukhtari.app.domain.repository.RegionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegionRepositoryImpl(
    private val regionDao: RegionDao
) : RegionRepository {
    override suspend fun getActiveRegions(): List<RegionEntity> = withContext(Dispatchers.IO) {
        regionDao.getActiveRegions()
    }

    override suspend fun getActiveRegionById(id: Long): RegionEntity? = withContext(Dispatchers.IO) {
        regionDao.getActiveRegionById(id)
    }

    override suspend fun saveRegion(region: RegionEntity): Long = withContext(Dispatchers.IO) {
        if (region.id == 0L) {
            regionDao.insertRegion(region)
        } else {
            regionDao.updateRegion(region)
            region.id
        }
    }

    override suspend fun softDeleteRegion(id: Long) = withContext(Dispatchers.IO) {
        regionDao.softDeleteRegion(id, System.currentTimeMillis(), "Deleted by user")
    }
}
