package com.mukhtari.app.ui.letters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.IncomingLetterEntity
import com.mukhtari.app.domain.repository.IncomingLetterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncomingLettersViewModel(
    private val incomingLetterRepository: IncomingLetterRepository
) : ViewModel() {

    private val _letters = MutableStateFlow<List<IncomingLetterEntity>>(emptyList())
    val letters: StateFlow<List<IncomingLetterEntity>> = _letters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLetters()
    }

    fun loadLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            incomingLetterRepository.getAllLetters().collect { result ->
                _letters.value = result
                _isLoading.value = false
            }
        }
    }

    fun saveLetter(letter: IncomingLetterEntity) {
        viewModelScope.launch {
            incomingLetterRepository.saveLetter(letter)
        }
    }

    fun deleteLetter(id: Long) {
        viewModelScope.launch {
            incomingLetterRepository.softDeleteLetter(id)
        }
    }
}
