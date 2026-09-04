package com.mukhtari.app.ui.letters

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
import com.mukhtari.app.data.local.entity.OutgoingLetterEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutgoingLettersScreen(
    onNavigateBack: () -> Unit,
    viewModel: OutgoingLettersViewModel = koinViewModel()
) {
    val letters by viewModel.letters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var letterToEdit by remember { mutableStateOf<OutgoingLetterEntity?>(null) }
    var publicCode by remember { mutableStateOf("") }
    var letterNumber by remember { mutableStateOf("") }
    var letterDate by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }

    if (showAddDialog || letterToEdit != null) {
        val isEdit = letterToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                letterToEdit = null
                publicCode = ""
                letterNumber = ""
                letterDate = ""
                recipient = ""
                subject = ""
            },
            title = { Text(if (isEdit) "تعديل كتاب صادر" else "إضافة كتاب صادر جديد") },
            text = {
                Column {
                    OutlinedTextField(
                        value = publicCode,
                        onValueChange = { publicCode = it },
                        label = { Text("الكود العام") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = letterNumber,
                        onValueChange = { letterNumber = it },
                        label = { Text("رقم الكتاب") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = letterDate,
                        onValueChange = { letterDate = it },
                        label = { Text("تاريخ الكتاب") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("الجهة المستلمة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("الموضوع") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (publicCode.isNotBlank() && letterNumber.isNotBlank() && letterDate.isNotBlank() && recipient.isNotBlank() && subject.isNotBlank()) {
                        val letterToSave = if (isEdit) {
                            letterToEdit!!.copy(
                                publicCode = publicCode,
                                letterNumber = letterNumber,
                                letterDate = letterDate,
                                recipient = recipient,
                                subject = subject,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            OutgoingLetterEntity(
                                publicCode = publicCode,
                                letterNumber = letterNumber,
                                letterDate = letterDate,
                                recipient = recipient,
                                subject = subject,
                                details = null,
                                recipientName = null,
                                deliveryDate = null,
                                deliveryMethod = null,
                                notes = null,
                                isDeleted = 0,
                                deletedAt = null,
                                deletedReason = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        }
                        viewModel.saveLetter(letterToSave)
                        showAddDialog = false
                        letterToEdit = null
                        publicCode = ""
                        letterNumber = ""
                        letterDate = ""
                        recipient = ""
                        subject = ""
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    letterToEdit = null
                    publicCode = ""
                    letterNumber = ""
                    letterDate = ""
                    recipient = ""
                    subject = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الكتب الصادرة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة كتاب")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (letters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد كتب صادرة.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(letters) { letter ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = letter.subject, style = MaterialTheme.typography.titleMedium)
                            Text(text = "رقم: ${letter.letterNumber} | إلى: ${letter.recipient}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    publicCode = letter.publicCode
                                    letterNumber = letter.letterNumber
                                    letterDate = letter.letterDate
                                    recipient = letter.recipient
                                    subject = letter.subject
                                    letterToEdit = letter
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(onClick = { viewModel.deleteLetter(letter.id) }) {
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
