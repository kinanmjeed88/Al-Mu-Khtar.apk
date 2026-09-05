package com.mukhtari.app.ui.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.ActivityLogEntity
import com.mukhtari.app.domain.repository.ActivityLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivityLogViewModel(
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<ActivityLogEntity>>(emptyList())
    val logs: StateFlow<List<ActivityLogEntity>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            activityLogRepository.getAllActivityLogs().collect { result ->
                _logs.value = result
                _isLoading.value = false
            }
        }
    }
}
