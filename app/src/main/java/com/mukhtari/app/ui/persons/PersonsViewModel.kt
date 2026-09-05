package com.mukhtari.app.ui.persons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.FamilyEntity
import com.mukhtari.app.data.local.entity.HouseEntity
import com.mukhtari.app.data.local.entity.PersonEntity
import com.mukhtari.app.domain.repository.FamilyRepository
import com.mukhtari.app.domain.repository.HouseRepository
import com.mukhtari.app.domain.repository.PersonRepository
import com.mukhtari.app.domain.usecase.ArabicNormalizationUseCase
import com.mukhtari.app.domain.usecase.DuplicateDetectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class PersonsViewModel(
    private val personRepository: PersonRepository,
    private val familyRepository: FamilyRepository,
    private val houseRepository: HouseRepository,
    private val duplicateDetectionUseCase: DuplicateDetectionUseCase,
    private val arabicNormalizationUseCase: ArabicNormalizationUseCase
) : ViewModel() {

    private val _persons = MutableStateFlow<List<PersonEntity>>(emptyList())
    val persons: StateFlow<List<PersonEntity>> = _persons.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _duplicateWarning = MutableStateFlow<String?>(null)
    val duplicateWarning: StateFlow<String?> = _duplicateWarning.asStateFlow()

    private val _families = MutableStateFlow<List<FamilyEntity>>(emptyList())
    val families: StateFlow<List<FamilyEntity>> = _families.asStateFlow()

    private val _houses = MutableStateFlow<List<HouseEntity>>(emptyList())
    val houses: StateFlow<List<HouseEntity>> = _houses.asStateFlow()

    private val _suggestedFamily = MutableStateFlow<FamilyEntity?>(null)
    val suggestedFamily: StateFlow<FamilyEntity?> = _suggestedFamily.asStateFlow()

    val searchQuery = MutableStateFlow("")

    private var suggestionJob: Job? = null

    init {
        loadPersons()
        loadFamiliesAndHouses()

        viewModelScope.launch {
            searchQuery.collect {
                loadPersons()
            }
        }
    }

    private fun loadFamiliesAndHouses() {
        viewModelScope.launch {
            familyRepository.getAllFamilies().collect {
                _families.value = it
            }
        }
        viewModelScope.launch {
            houseRepository.getAllHouses().collect {
                _houses.value = it
            }
        }
    }

    fun validateAndSavePerson(person: PersonEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (person.familyId == null) {
                onResult(false, "يجب اختيار العائلة")
                return@launch
            }

            val family = familyRepository.getFamilyById(person.familyId)
            if (family == null || family.isDeleted == 1) {
                onResult(false, "العائلة المحددة غير موجودة أو محذوفة")
                return@launch
            }

            if (family.houseId != person.houseId) {
                onResult(false, "لا يمكن اختيار دار مختلفة عن دار العائلة")
                return@launch
            }

            val house = houseRepository.getHouseById(person.houseId!!)
            if (house == null || house.isDeleted == 1) {
                onResult(false, "الدار المرتبطة بالعائلة غير موجودة أو محذوفة")
                return@launch
            }

            savePerson(person)
            onResult(true, null)
        }
    }

    fun loadPersons() {
        viewModelScope.launch {
            _isLoading.value = true
            val query = searchQuery.value.trim()
            if (query.isEmpty()) {
                _persons.value = personRepository.getActivePersons()
            } else {
                _persons.value = personRepository.searchPersons(query)
            }
            _isLoading.value = false
        }
    }

    fun savePerson(person: PersonEntity) {
        viewModelScope.launch {
            personRepository.savePerson(person)
            loadPersons()
        }
    }

    fun deletePerson(id: Long) {
        viewModelScope.launch {
            personRepository.softDeletePerson(id)
            loadPersons()
        }
    }

    fun checkForDuplicates(candidate: PersonEntity) {
        viewModelScope.launch {
            val allPersons = personRepository.getActivePersons()
            var maxScore = 0
            for (target in allPersons) {
                // Ignore self if editing
                if (target.id == candidate.id) continue

                val score = duplicateDetectionUseCase.calculateDuplicateScore(target, candidate)
                if (score > maxScore) {
                    maxScore = score
                }
            }
            if (maxScore >= 40) {
                _duplicateWarning.value = "تحذير: يوجد شخص مشابه مسجل مسبقاً (نسبة التطابق مرتفعة)."
            } else {
                _duplicateWarning.value = null
            }
        }
    }

    fun clearDuplicateWarning() {
        _duplicateWarning.value = null
    }

    fun suggestFamilyForPerson(fullName: String) {
        suggestionJob?.cancel()

        if (fullName.isBlank()) {
            _suggestedFamily.value = null
            return
        }

        suggestionJob = viewModelScope.launch {
            delay(500) // Debounce

            val normalizedSearch = arabicNormalizationUseCase(fullName)
            // Split name to find father/grandfather part
            val nameParts = normalizedSearch.split(" ")

            if (nameParts.size < 2) {
                _suggestedFamily.value = null
                return@launch
            }

            // Try matching father/grandfather name from the input against family head names or family names
            val searchTarget = nameParts.drop(1).joinToString(" ")

            var matchedFamily: FamilyEntity? = null
            val familiesList = _families.value

            for (family in familiesList) {
                if (family.headOfFamilyId != null) {
                    val head = personRepository.getActivePersonById(family.headOfFamilyId)
                    if (head != null) {
                        val normalizedHeadName = arabicNormalizationUseCase(head.fullName)
                        if (normalizedHeadName.contains(searchTarget)) {
                            matchedFamily = family
                            break
                        }
                    }
                }

                // Fallback check against family name
                if (family.familyName != null) {
                    val normalizedFamilyName = arabicNormalizationUseCase(family.familyName)
                    if (normalizedFamilyName.contains(searchTarget)) {
                        matchedFamily = family
                        break
                    }
                }
            }

            _suggestedFamily.value = matchedFamily
        }
    }

    fun clearSuggestedFamily() {
        _suggestedFamily.value = null
    }
}
