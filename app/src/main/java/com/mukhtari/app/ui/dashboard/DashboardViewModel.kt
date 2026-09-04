package com.mukhtari.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.domain.repository.DashboardRepository
import com.mukhtari.app.domain.repository.DashboardStatistics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    dashboardRepository: DashboardRepository
) : ViewModel() {

    val statistics: StateFlow<DashboardStatistics?> = dashboardRepository.getDashboardStatistics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
