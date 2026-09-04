package com.mukhtari.app.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mukhtari.app.data.local.entity.TransactionEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = koinViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionCode by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("") }
    var requestDate by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    if (showAddDialog || transactionToEdit != null) {
        val isEdit = transactionToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                transactionToEdit = null
                transactionCode = ""
                transactionType = ""
                requestDate = ""
                subject = ""
                status = ""
            },
            title = { Text(if (isEdit) "تعديل المعاملة" else "إضافة معاملة جديدة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = transactionCode,
                        onValueChange = { transactionCode = it },
                        label = { Text("رمز المعاملة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = transactionType,
                        onValueChange = { transactionType = it },
                        label = { Text("نوع المعاملة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = requestDate,
                        onValueChange = { requestDate = it },
                        label = { Text("تاريخ الطلب") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("الموضوع") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("الحالة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (transactionCode.isNotBlank() && transactionType.isNotBlank() && requestDate.isNotBlank() && subject.isNotBlank() && status.isNotBlank()) {
                        val transactionToSave = if (isEdit) {
                            transactionToEdit!!.copy(
                                transactionCode = transactionCode,
                                transactionType = transactionType,
                                requestDate = requestDate,
                                subject = subject,
                                status = status,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            TransactionEntity(
                                transactionCode = transactionCode,
                                transactionType = transactionType,
                                requestDate = requestDate,
                                subject = subject,
                                status = status,
                                personId = null,
                                familyId = null,
                                applicantNameSnapshot = null,
                                details = null,
                                notes = null,
                                isDeleted = 0,
                                deletedAt = null,
                                deletedReason = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        }
                        viewModel.saveTransaction(transactionToSave)
                        showAddDialog = false
                        transactionToEdit = null
                        transactionCode = ""
                        transactionType = ""
                        requestDate = ""
                        subject = ""
                        status = ""
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    transactionToEdit = null
                    transactionCode = ""
                    transactionType = ""
                    requestDate = ""
                    subject = ""
                    status = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المعاملات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة معاملة")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد معاملات مضافة.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(transactions) { transaction ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = transaction.subject, style = MaterialTheme.typography.titleMedium)
                            Text(text = "رمز: ${transaction.transactionCode} | حالة: ${transaction.status}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    transactionCode = transaction.transactionCode
                                    transactionType = transaction.transactionType
                                    requestDate = transaction.requestDate
                                    subject = transaction.subject
                                    status = transaction.status
                                    transactionToEdit = transaction
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(onClick = { viewModel.deleteTransaction(transaction.id) }) {
                                    Text("حذف", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
