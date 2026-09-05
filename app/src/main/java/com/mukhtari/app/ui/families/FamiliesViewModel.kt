package com.mukhtari.app.ui.families

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.FamilyEntity
import com.mukhtari.app.data.local.entity.HouseEntity
import com.mukhtari.app.data.local.entity.PersonEntity
import com.mukhtari.app.domain.repository.FamilyRepository
import com.mukhtari.app.domain.repository.HouseRepository
import com.mukhtari.app.domain.repository.PersonRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FamiliesViewModel(
    private val familyRepository: FamilyRepository,
    private val houseRepository: HouseRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _families = MutableStateFlow<List<FamilyEntity>>(emptyList())
    val families: StateFlow<List<FamilyEntity>> = _families.asStateFlow()

    private val _houses = MutableStateFlow<List<HouseEntity>>(emptyList())
    val houses: StateFlow<List<HouseEntity>> = _houses.asStateFlow()

    private val _familyPersons = MutableStateFlow<List<PersonEntity>>(emptyList())
    val familyPersons: StateFlow<List<PersonEntity>> = _familyPersons.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFamilies()
        loadHouses()
    }

    private fun loadHouses() {
        viewModelScope.launch {
            houseRepository.getAllHouses().collect { result ->
                _houses.value = result
            }
        }
    }

    fun loadPersonsForFamily(familyId: Long) {
        viewModelScope.launch {
            _familyPersons.value = personRepository.getPersonsByFamilyId(familyId)
        }
    }

    fun clearFamilyPersons() {
        _familyPersons.value = emptyList()
    }

    fun validateAndSaveFamily(family: FamilyEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (family.houseId == null) {
                onResult(false, "يجب اختيار الدار")
                return@launch
            }

            val house = houseRepository.getHouseById(family.houseId)
            if (house == null || house.isDeleted == 1) {
                onResult(false, "الدار المحددة غير موجودة أو محذوفة")
                return@launch
            }

            if (family.headOfFamilyId != null) {
                val head = personRepository.getActivePersonById(family.headOfFamilyId)
                if (head == null || head.isDeleted == 1) {
                    onResult(false, "رب الأسرة المحدد غير موجود أو محذوف")
                    return@launch
                }
                if (head.familyId != family.id && family.id != 0L) {
                    onResult(false, "رب الأسرة يجب أن يكون من ضمن أفراد العائلة")
                    return@launch
                }
            }

            saveFamily(family)
            onResult(true, null)
        }
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

    fun createAndSelectHouse(houseNumber: String, onSelect: (Long) -> Unit) {
        viewModelScope.launch {
            val uuidStr = java.util.UUID.randomUUID().toString().take(8).uppercase()
            val newHouse = HouseEntity(
                publicCode = "HSE-$uuidStr",
                internalNumber = "HSE-$uuidStr",
                houseNumber = houseNumber,
                streetId = null,
                alleyId = null,
                mahallaNumber = null,
                detailedAddress = null,
                photoPath = null,
                propertyType = "owned",
                status = "occupied",
                ownershipType = "owned",
                ownerName = null,
                ownerPhone = null,
                notes = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                deletedAt = null,
                deletedReason = null
            )
            val houseId = houseRepository.saveHouse(newHouse)
            loadHouses()
            onSelect(houseId)
        }
    }
}
