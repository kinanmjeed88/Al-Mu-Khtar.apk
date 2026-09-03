package com.mukhtari.app.data.repository

import com.mukhtari.app.data.local.dao.DashboardDao
import com.mukhtari.app.domain.repository.DashboardRepository
import com.mukhtari.app.domain.repository.DashboardStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardRepositoryImpl(
    private val dashboardDao: DashboardDao
) : DashboardRepository {

    override fun getDashboardStatistics(): Flow<DashboardStatistics> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1) // Last 30 days essentially
        val dateThreshold = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

        val flow1 = combine(
            dashboardDao.getTotalHouses(),
            dashboardDao.getOccupiedHouses(),
            dashboardDao.getVacantHouses(),
            dashboardDao.getTotalFamilies(),
            dashboardDao.getTotalPersons()
        ) { h, oh, vh, f, p -> intArrayOf(h, oh, vh, f, p) }

        val flow2 = combine(
            dashboardDao.getTotalMales(),
            dashboardDao.getTotalFemales(),
            dashboardDao.getNewArrivals(dateThreshold),
            dashboardDao.getRecentDepartures(dateThreshold)
        ) { tm, tf, na, rd -> intArrayOf(tm, tf, na, rd) }

        return combine(flow1, flow2) { arr1, arr2 ->
            DashboardStatistics(
                totalHouses = arr1[0],
                occupiedHouses = arr1[1],
                vacantHouses = arr1[2],
                totalFamilies = arr1[3],
                totalPersons = arr1[4],
                totalMales = arr2[0],
                totalFemales = arr2[1],
                newArrivals = arr2[2],
                recentDepartures = arr2[3]
            )
        }
    }
}
