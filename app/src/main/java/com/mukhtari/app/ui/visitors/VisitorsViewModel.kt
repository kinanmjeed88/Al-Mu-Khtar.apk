package com.mukhtari.app.ui.visitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.VisitorLogEntity
import com.mukhtari.app.domain.repository.VisitorLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisitorsViewModel(
    private val visitorLogRepository: VisitorLogRepository
) : ViewModel() {

    private val _visitors = MutableStateFlow<List<VisitorLogEntity>>(emptyList())
    val visitors: StateFlow<List<VisitorLogEntity>> = _visitors.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadVisitors()
    }

    fun loadVisitors() {
        viewModelScope.launch {
            _isLoading.value = true
            visitorLogRepository.getAllVisitorLogs().collect { result ->
                _visitors.value = result
                _isLoading.value = false
            }
        }
    }

    fun saveVisitor(visitor: VisitorLogEntity) {
        viewModelScope.launch {
            visitorLogRepository.saveVisitorLog(visitor)
        }
    }
}
