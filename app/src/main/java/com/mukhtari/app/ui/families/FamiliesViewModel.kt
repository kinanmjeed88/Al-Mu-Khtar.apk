package com.mukhtari.app.ui.families

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.FamilyEntity
import com.mukhtari.app.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FamiliesViewModel(
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _families = MutableStateFlow<List<FamilyEntity>>(emptyList())
    val families: StateFlow<List<FamilyEntity>> = _families.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFamilies()
    }

    fun loadFamilies() {
        viewModelScope.launch {
            _isLoading.value = true
            familyRepository.getAllFamilies().collect { result ->
                _families.value = result
                _isLoading.value = false
            }
        }
    }

    fun saveFamily(family: FamilyEntity) {
        viewModelScope.launch {
            familyRepository.saveFamily(family)
        }
    }

    fun deleteFamily(id: Long) {
        viewModelScope.launch {
            familyRepository.softDeleteFamily(id)
        }
    }
}
