package com.mukhtari.app.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isPinSet = MutableStateFlow(false)
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        checkPinStatus()
    }

    private fun checkPinStatus() {
        viewModelScope.launch {
            val hasPin = securityRepository.isPinSet()
            _isPinSet.value = hasPin
            if (!hasPin) {
                _isLocked.value = false
            }
        }
    }

    fun setPin(pin: String) {
        if (pin.length < 4) {
            _error.value = "الرمز يجب أن يكون 4 أرقام على الأقل"
            return
        }
        viewModelScope.launch {
            securityRepository.setPin(pin)
            _isPinSet.value = true
            _isLocked.value = false
            _error.value = null
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            if (securityRepository.verifyPin(pin)) {
                _isLocked.value = false
                _error.value = null
            } else {
                _error.value = "رمز خاطئ"
            }
        }
    }
}
