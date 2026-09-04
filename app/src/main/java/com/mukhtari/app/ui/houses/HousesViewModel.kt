package com.mukhtari.app.ui.houses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.HouseEntity
import com.mukhtari.app.domain.repository.HouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HousesViewModel(
    private val houseRepository: HouseRepository
) : ViewModel() {

    private val _houses = MutableStateFlow<List<HouseEntity>>(emptyList())
    val houses: StateFlow<List<HouseEntity>> = _houses.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHouses()
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
