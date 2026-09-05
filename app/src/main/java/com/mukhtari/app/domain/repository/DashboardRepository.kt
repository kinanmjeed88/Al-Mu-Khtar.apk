package com.mukhtari.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class DashboardStatistics(
    val totalHouses: Int,
    val occupiedHouses: Int,
    val vacantHouses: Int,
    val totalFamilies: Int,
    val totalPersons: Int,
    val totalMales: Int,
    val totalFemales: Int,
    val newArrivals: Int,
    val recentDepartures: Int,
    val totalTransactions: Int
)

interface DashboardRepository {
    fun getDashboardStatistics(): Flow<DashboardStatistics>
}
