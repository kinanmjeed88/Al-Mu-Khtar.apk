package com.mukhtari.app.ui.persons

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
import com.mukhtari.app.data.local.entity.PersonEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonsViewModel = koinViewModel()
) {
    val persons by viewModel.persons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val duplicateWarning by viewModel.duplicateWarning.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }
    var publicCode by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }

    if (showAddDialog || personToEdit != null) {
        val isEdit = personToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                personToEdit = null
                publicCode = ""
                fullName = ""
                fatherName = ""
                viewModel.clearDuplicateWarning()
            },
            title = { Text(if (isEdit) "تعديل الفرد" else "إضافة فرد جديد") },
            text = {
                Column {
                    OutlinedTextField(
                        value = publicCode,
                        onValueChange = { publicCode = it },
                        label = { Text("الكود العام / رقم الهوية") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            val candidate = PersonEntity(
                                id = personToEdit?.id ?: 0L,
                                publicCode = publicCode,
                                fullName = fullName,
                                fatherName = fatherName,
                                grandfatherName = null,
                                surname = null,
                                gender = "male",
                                birthDate = null,
                                maritalStatus = "single",
                                relationToHead = "self",
                                familyId = null,
                                houseId = null,
                                workStatus = "unemployed",
                                employer = null,
                                jobTitle = null,
                                educationLevel = "none",
                                phone = null,
                                phoneAlt = null,
                                notes = null,
                                isDeleted = 0,
                                deletedAt = null,
                                deletedReason = null,
                                createdAt = 0,
                                updatedAt = 0
                            )
                            viewModel.checkForDuplicates(candidate)
                        },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("اسم الأب") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    if (duplicateWarning != null) {
                        Text(
                            text = duplicateWarning!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (publicCode.isNotBlank() && fullName.isNotBlank()) {
                        val personToSave = if (isEdit) {
                            personToEdit!!.copy(
                                publicCode = publicCode,
                                fullName = fullName,
                                fatherName = fatherName,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            PersonEntity(
                                publicCode = publicCode,
                                fullName = fullName,
                                fatherName = fatherName,
                                grandfatherName = null,
                                surname = null,
                                gender = "male",
                                birthDate = null,
                                maritalStatus = "single",
                                relationToHead = "self",
                                familyId = null,
                                houseId = null,
                                workStatus = "unemployed",
                                employer = null,
                                jobTitle = null,
                                educationLevel = "none",
                                phone = null,
                                phoneAlt = null,
                                notes = null,
                                isDeleted = 0,
                                deletedAt = null,
                                deletedReason = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        }
                        viewModel.savePerson(personToSave)
                        showAddDialog = false
                        personToEdit = null
                        publicCode = ""
                        fullName = ""
                        fatherName = ""
                        viewModel.clearDuplicateWarning()
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    personToEdit = null
                    publicCode = ""
                    fullName = ""
                    fatherName = ""
                    viewModel.clearDuplicateWarning()
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الأفراد") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة فرد")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (persons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد أفراد مضافين.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(persons) { person ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = person.fullName, style = MaterialTheme.typography.titleMedium)
                            Text(text = "رقم الهوية: ${person.publicCode}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    publicCode = person.publicCode
                                    fullName = person.fullName
                                    fatherName = person.fatherName ?: ""
                                    personToEdit = person
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(onClick = { viewModel.deletePerson(person.id) }) {
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
