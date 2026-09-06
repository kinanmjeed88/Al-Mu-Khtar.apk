package com.mukhtari.app.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.domain.repository.BackupRestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class BackupRestoreViewModel(
    private val backupRestoreRepository: BackupRestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupRestoreUiState>(BackupRestoreUiState.Idle)
    val uiState: StateFlow<BackupRestoreUiState> = _uiState

    fun createBackup(outputDir: File) {
        viewModelScope.launch {
            _uiState.value = BackupRestoreUiState.Loading
            try {
                val backupFile = backupRestoreRepository.createBackup(outputDir)
                if (backupFile.exists()) {
                    _uiState.value = BackupRestoreUiState.Success("تم إنشاء النسخة الاحتياطية بنجاح: \n${backupFile.absolutePath}")
                } else {
                    _uiState.value = BackupRestoreUiState.Error("فشل إنشاء النسخة الاحتياطية")
                }
            } catch (e: Exception) {
                _uiState.value = BackupRestoreUiState.Error("حدث خطأ: ${e.message}")
            }
        }
    }

    fun restoreBackup(backupFile: File) {
        viewModelScope.launch {
            _uiState.value = BackupRestoreUiState.Loading
            try {
                val success = backupRestoreRepository.restoreBackup(backupFile)
                if (success) {
                    _uiState.value = BackupRestoreUiState.Success("تم استعادة النسخة الاحتياطية بنجاح")
                } else {
                    _uiState.value = BackupRestoreUiState.Error("فشل استعادة النسخة الاحتياطية")
                }
            } catch (e: Exception) {
                _uiState.value = BackupRestoreUiState.Error("حدث خطأ: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = BackupRestoreUiState.Idle
    }
}

sealed class BackupRestoreUiState {
    object Idle : BackupRestoreUiState()
    object Loading : BackupRestoreUiState()
    data class Success(val message: String) : BackupRestoreUiState()
    data class Error(val message: String) : BackupRestoreUiState()
}
