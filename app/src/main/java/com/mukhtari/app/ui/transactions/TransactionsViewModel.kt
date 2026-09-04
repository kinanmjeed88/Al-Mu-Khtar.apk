package com.mukhtari.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.TransactionEntity
import com.mukhtari.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            transactionRepository.getAllTransactions().collect { result ->
                _transactions.value = result
                _isLoading.value = false
            }
        }
    }

    fun saveTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.saveTransaction(transaction)
            // collect updates automatically since it's a flow, but no harm in not reloading manually if collected
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(id)
        }
    }
}
