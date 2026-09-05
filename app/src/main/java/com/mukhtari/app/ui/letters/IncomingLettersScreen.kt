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
import com.mukhtari.app.data.local.entity.IncomingLetterEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingLettersScreen(
    onNavigateBack: () -> Unit,
    viewModel: IncomingLettersViewModel = koinViewModel()
) {
    val letters by viewModel.letters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var letterToEdit by remember { mutableStateOf<IncomingLetterEntity?>(null) }
    var letterNumber by remember { mutableStateOf("") }
    var letterDate by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    if (showAddDialog || letterToEdit != null) {
        val isEdit = letterToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                letterToEdit = null
                letterNumber = ""
                letterDate = ""
                sender = ""
                subject = ""
                status = ""
            },
            title = { Text(if (isEdit) "تعديل كتاب وارد" else "إضافة كتاب وارد جديد") },
            text = {
                Column {
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
                        value = sender,
                        onValueChange = { sender = it },
                        label = { Text("الجهة المرسلة") },
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
                    if (letterNumber.isNotBlank() && letterDate.isNotBlank() && sender.isNotBlank() && subject.isNotBlank() && status.isNotBlank()) {
                        val letterToSave = if (isEdit) {
                            letterToEdit!!.copy(
                                letterNumber = letterNumber,
                                letterDate = letterDate,
                                sender = sender,
                                subject = subject,
                                status = status,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            IncomingLetterEntity(
                                publicCode = "",
                                letterNumber = letterNumber,
                                letterDate = letterDate,
                                sender = sender,
                                subject = subject,
                                status = status,
                                details = null,
                                requiredAction = null,
                                actionDate = null,
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
                        letterNumber = ""
                        letterDate = ""
                        sender = ""
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
                    letterToEdit = null
                    letterNumber = ""
                    letterDate = ""
                    sender = ""
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
                title = { Text("الكتب الواردة") },
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
                Text("لا توجد كتب واردة.", style = MaterialTheme.typography.bodyLarge)
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
                            Text(text = "رقم: ${letter.letterNumber} | من: ${letter.sender}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    letterNumber = letter.letterNumber
                                    letterDate = letter.letterDate
                                    sender = letter.sender
                                    subject = letter.subject
                                    status = letter.status
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
