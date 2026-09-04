package com.mukhtari.app.ui.letters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.OutgoingLetterEntity
import com.mukhtari.app.domain.repository.OutgoingLetterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OutgoingLettersViewModel(
    private val outgoingLetterRepository: OutgoingLetterRepository
) : ViewModel() {

    private val _letters = MutableStateFlow<List<OutgoingLetterEntity>>(emptyList())
    val letters: StateFlow<List<OutgoingLetterEntity>> = _letters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLetters()
    }

    fun loadLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            outgoingLetterRepository.getAllLetters().collect { result ->
                _letters.value = result
                _isLoading.value = false
            }
        }
    }

    fun saveLetter(letter: OutgoingLetterEntity) {
        viewModelScope.launch {
            outgoingLetterRepository.saveLetter(letter)
        }
    }

    fun deleteLetter(id: Long) {
        viewModelScope.launch {
            outgoingLetterRepository.softDeleteLetter(id)
        }
    }
}
