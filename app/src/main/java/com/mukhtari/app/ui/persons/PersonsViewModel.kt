package com.mukhtari.app.ui.persons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.PersonEntity
import com.mukhtari.app.domain.repository.PersonRepository
import com.mukhtari.app.domain.usecase.DuplicateDetectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonsViewModel(
    private val personRepository: PersonRepository,
    private val duplicateDetectionUseCase: DuplicateDetectionUseCase
) : ViewModel() {

    private val _persons = MutableStateFlow<List<PersonEntity>>(emptyList())
    val persons: StateFlow<List<PersonEntity>> = _persons.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _duplicateWarning = MutableStateFlow<String?>(null)
    val duplicateWarning: StateFlow<String?> = _duplicateWarning.asStateFlow()

    val searchQuery = MutableStateFlow("")

    init {
        loadPersons()

        viewModelScope.launch {
            searchQuery.collect {
                loadPersons()
            }
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
}
