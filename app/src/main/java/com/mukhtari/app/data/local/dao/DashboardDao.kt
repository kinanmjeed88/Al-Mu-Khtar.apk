package com.mukhtari.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT COUNT(*) FROM houses WHERE is_deleted = 0")
    fun getTotalHouses(): Flow<Int>

    @Query("SELECT COUNT(*) FROM houses WHERE status = 'occupied' AND is_deleted = 0")
    fun getOccupiedHouses(): Flow<Int>

    @Query("SELECT COUNT(*) FROM houses WHERE status = 'vacant' AND is_deleted = 0")
    fun getVacantHouses(): Flow<Int>

    @Query("SELECT COUNT(*) FROM families WHERE is_deleted = 0")
    fun getTotalFamilies(): Flow<Int>

    @Query("SELECT COUNT(*) FROM persons WHERE is_deleted = 0")
    fun getTotalPersons(): Flow<Int>

    @Query("SELECT COUNT(*) FROM persons WHERE gender = 'ذكر' AND is_deleted = 0")
    fun getTotalMales(): Flow<Int>

    @Query("SELECT COUNT(*) FROM persons WHERE gender = 'أنثى' AND is_deleted = 0")
    fun getTotalFemales(): Flow<Int>

    @Query("SELECT COUNT(*) FROM residencies WHERE residency_type = 'incoming' AND start_date > :timestampThreshold")
    fun getNewArrivals(timestampThreshold: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM residencies WHERE (end_date IS NOT NULL AND end_date != '') AND end_date > :timestampThreshold")
    fun getRecentDepartures(timestampThreshold: String): Flow<Int>
}
