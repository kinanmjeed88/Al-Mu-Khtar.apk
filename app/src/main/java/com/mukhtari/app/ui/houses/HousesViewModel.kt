package com.mukhtari.app.ui.houses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.AlleyEntity
import com.mukhtari.app.data.local.entity.HouseEntity
import com.mukhtari.app.data.local.entity.RegionEntity
import com.mukhtari.app.data.local.entity.StreetEntity
import com.mukhtari.app.domain.repository.AlleyRepository
import com.mukhtari.app.domain.repository.HouseRepository
import com.mukhtari.app.domain.repository.RegionRepository
import com.mukhtari.app.domain.repository.StreetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HousesViewModel(
    private val houseRepository: HouseRepository,
    private val regionRepository: RegionRepository,
    private val streetRepository: StreetRepository,
    private val alleyRepository: AlleyRepository
) : ViewModel() {

    private val _houses = MutableStateFlow<List<HouseEntity>>(emptyList())
    val houses: StateFlow<List<HouseEntity>> = _houses.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _regions = MutableStateFlow<List<RegionEntity>>(emptyList())
    val regions: StateFlow<List<RegionEntity>> = _regions.asStateFlow()

    private val _streets = MutableStateFlow<List<StreetEntity>>(emptyList())
    val streets: StateFlow<List<StreetEntity>> = _streets.asStateFlow()

    private val _alleys = MutableStateFlow<List<AlleyEntity>>(emptyList())
    val alleys: StateFlow<List<AlleyEntity>> = _alleys.asStateFlow()

    private var streetsJob: Job? = null
    private var alleysJob: Job? = null

    init {
        loadHouses()
        loadRegions()
    }

    private fun loadRegions() {
        viewModelScope.launch {
            _regions.value = regionRepository.getActiveRegions()
        }
    }

    fun loadStreetsForRegion(regionId: Long) {
        streetsJob?.cancel()
        _streets.value = emptyList()
        _alleys.value = emptyList()
        streetsJob = viewModelScope.launch {
            streetRepository.getStreetsForRegion(regionId).collect {
                _streets.value = it
            }
        }
    }

    fun loadAlleysForStreet(streetId: Long) {
        alleysJob?.cancel()
        _alleys.value = emptyList()
        alleysJob = viewModelScope.launch {
            alleyRepository.getAlleysForStreet(streetId).collect {
                _alleys.value = it
            }
        }
    }

    fun clearDependentSelections() {
        streetsJob?.cancel()
        alleysJob?.cancel()
        _streets.value = emptyList()
        _alleys.value = emptyList()
    }

    fun validateAndSaveHouse(house: HouseEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            // Basic validation
            if (house.streetId == null) {
                onResult(false, "يجب اختيار الشارع")
                return@launch
            }
            if (house.alleyId == null) {
                onResult(false, "يجب اختيار الزقاق")
                return@launch
            }

            // Cross-validation with database
            val street = streetRepository.getStreetById(house.streetId)
            if (street == null || street.isDeleted == 1) {
                onResult(false, "الشارع المحدد غير موجود أو محذوف")
                return@launch
            }

            val alley = alleyRepository.getAlleyById(house.alleyId)
            if (alley == null || alley.isDeleted == 1) {
                onResult(false, "الزقاق المحدد غير موجود أو محذوف")
                return@launch
            }

            if (alley.streetId != street.id) {
                onResult(false, "الزقاق المحدد لا يتبع للشارع المحدد")
                return@launch
            }

            saveHouse(house)
            onResult(true, null)
        }
    }

    fun loadHouses() {
        viewModelScope.launch {
            _isLoading.value = true
            houseRepository.getAllHouses().collect { result ->
                _houses.value = result
                _isLoading.value = false
            }
        }
    }

    fun saveHouse(house: HouseEntity) {
        viewModelScope.launch {
            houseRepository.saveHouse(house)
        }
    }

    fun deleteHouse(id: Long) {
        viewModelScope.launch {
            houseRepository.softDeleteHouse(id)
        }
    }
}
