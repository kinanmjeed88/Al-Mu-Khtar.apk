package com.mukhtari.app.ui.regions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.RegionEntity
import com.mukhtari.app.domain.repository.RegionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegionsViewModel(
    private val regionRepository: RegionRepository
) : ViewModel() {

    private val _regions = MutableStateFlow<List<RegionEntity>>(emptyList())
    val regions: StateFlow<List<RegionEntity>> = _regions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRegions()
    }

    fun loadRegions() {
        viewModelScope.launch {
            _isLoading.value = true
            _regions.value = regionRepository.getActiveRegions()
            _isLoading.value = false
        }
    }

    fun saveRegion(region: RegionEntity) {
        viewModelScope.launch {
            regionRepository.saveRegion(region)
            loadRegions()
        }
    }

    fun deleteRegion(id: Long) {
        viewModelScope.launch {
            regionRepository.softDeleteRegion(id)
            loadRegions()
        }
    }
}
