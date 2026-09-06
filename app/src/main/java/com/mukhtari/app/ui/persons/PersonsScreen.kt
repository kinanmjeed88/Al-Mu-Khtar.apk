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
import androidx.compose.foundation.clickable
import com.mukhtari.app.data.local.entity.PersonEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCertificates: (Long, String) -> Unit = { _, _ -> },
    onNavigateToAttachments: (String, Long) -> Unit,
    viewModel: PersonsViewModel = koinViewModel()
) {
    val persons by viewModel.persons.collectAsState()
    val families by viewModel.families.collectAsState()
    val houses by viewModel.houses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val duplicateWarning by viewModel.duplicateWarning.collectAsState()
    val suggestedFamily by viewModel.suggestedFamily.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }
    var fullName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var birthDateString by remember { mutableStateOf("") }
    var selectedFamilyId by remember { mutableStateOf<Long?>(null) }
    var selectedHouseId by remember { mutableStateOf<Long?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var expandedFamily by remember { mutableStateOf(false) }

    var showMergeDialog by remember { mutableStateOf(false) }
    var mergeTargetId by remember { mutableStateOf<Long?>(null) }

    var showAddFamilyDialog by remember { mutableStateOf(false) }
    var newFamilyName by remember { mutableStateOf("") }

    if (showAddDialog || personToEdit != null) {
        val isEdit = personToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                personToEdit = null
                fullName = ""
                fatherName = ""
                birthDateString = ""
                selectedFamilyId = null
                selectedHouseId = null
                errorMsg = null
                viewModel.clearDuplicateWarning()
                viewModel.clearSuggestedFamily()
            },
            title = { Text(if (isEdit) "تعديل الفرد" else "إضافة فرد جديد") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedFamily,
                            onExpandedChange = { expandedFamily = !expandedFamily },
                            modifier = Modifier.weight(1f)
                        ) {
                            val selectedFamilyName = families.find { it.id == selectedFamilyId }?.familyName ?: "اختر العائلة"
                            OutlinedTextField(
                                value = selectedFamilyName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("العائلة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFamily) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFamily,
                                onDismissRequest = { expandedFamily = false }
                            ) {
                                families.forEach { family ->
                                    DropdownMenuItem(
                                        text = { Text(family.familyName ?: "عائلة ${family.familyCode}") },
                                        onClick = {
                                            selectedFamilyId = family.id
                                            selectedHouseId = family.houseId
                                            expandedFamily = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { showAddFamilyDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة عائلة")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val houseDisplay = houses.find { it.id == selectedHouseId }?.houseNumber ?: "لا يوجد دار"
                    OutlinedTextField(
                        value = houseDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الدار التابعة للعائلة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        enabled = false
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            val candidate = PersonEntity(
                                id = personToEdit?.id ?: 0L,
                                publicCode = personToEdit?.publicCode ?: "",
                                fullName = fullName,
                                fatherName = fatherName,
                                grandfatherName = null,
                                surname = null,
                                gender = "unknown",
                                birthDate = birthDateString.ifBlank { null },
                                maritalStatus = "unknown",
                                relationToHead = "unknown",
                                familyId = selectedFamilyId,
                                houseId = selectedHouseId,
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
                            viewModel.suggestFamilyForPerson(fullName)
                        },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    if (suggestedFamily != null) {
                        Surface(
                            onClick = {
                                selectedFamilyId = suggestedFamily?.id
                                selectedHouseId = suggestedFamily?.houseId
                                viewModel.clearSuggestedFamily()
                            },
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = "اقتراح")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("العائلة المقترحة: ${suggestedFamily?.familyName ?: suggestedFamily?.familyCode}")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("اسم الأب") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = birthDateString,
                        onValueChange = {
                            birthDateString = it
                        },
                        label = { Text("تاريخ الميلاد (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    val ageText = try {
                        if (birthDateString.isNotBlank()) {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val birthDate = sdf.parse(birthDateString)
                            if (birthDate != null) {
                                val diffMillis = System.currentTimeMillis() - birthDate.time
                                val ageYears = diffMillis / (1000L * 60 * 60 * 24 * 365)
                                if (ageYears >= 0) {
                                    "العمر: $ageYears سنة"
                                } else null
                            } else null
                        } else null
                    } catch (e: Exception) {
                        null
                    }

                    if (ageText != null) {
                         Text(text = ageText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    if (duplicateWarning != null) {
                        Text(
                            text = duplicateWarning ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        // Trigger merge via picking another person temporarily using search or just explicit UI if advanced
                        // Since this is basic duplicate detection on save, if it's editing, they could merge
                        if (isEdit) {
                            TextButton(onClick = { showMergeDialog = true }) {
                                Text("دمج مع سجل مشابه؟", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (fullName.isNotBlank() && selectedFamilyId != null) {
                        val personToSave = if (isEdit) {
                            personToEdit!!.copy(
                                fullName = fullName,
                                fatherName = fatherName,
                                birthDate = birthDateString.ifBlank { null },
                                familyId = selectedFamilyId,
                                houseId = selectedHouseId,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            PersonEntity(
                                publicCode = "",
                                fullName = fullName,
                                fatherName = fatherName,
                                grandfatherName = null,
                                surname = null,
                                gender = "unknown",
                                birthDate = birthDateString.ifBlank { null },
                                maritalStatus = "unknown",
                                relationToHead = "unknown",
                                familyId = selectedFamilyId,
                                houseId = selectedHouseId,
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

                        viewModel.validateAndSavePerson(personToSave) { success, error ->
                            if (success) {
                                showAddDialog = false
                                personToEdit = null
                                fullName = ""
                                fatherName = ""
                                birthDateString = ""
                                selectedFamilyId = null
                                selectedHouseId = null
                                errorMsg = null
                                viewModel.clearDuplicateWarning()
                                viewModel.clearSuggestedFamily()
                            } else {
                                errorMsg = error
                            }
                        }
                    } else {
                        errorMsg = "يرجى تعبئة جميع الحقول المطلوبة واختيار العائلة"
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    personToEdit = null
                    fullName = ""
                    fatherName = ""
                    birthDateString = ""
                    selectedFamilyId = null
                    selectedHouseId = null
                    errorMsg = null
                    viewModel.clearDuplicateWarning()
                    viewModel.clearSuggestedFamily()
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showMergeDialog && personToEdit != null) {
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("دمج الأفراد") },
            text = {
                Column {
                    Text("اختر السجل الأساسي الذي ترغب بدمج السجل الحالي (${personToEdit!!.fullName}) إليه:")
                    Spacer(modifier = Modifier.height(8.dp))

                    var mergeSearchQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = mergeSearchQuery,
                        onValueChange = { mergeSearchQuery = it },
                        label = { Text("ابحث عن الشخص الأساسي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredMergePersons = persons.filter {
                        it.id != personToEdit!!.id && it.fullName.contains(mergeSearchQuery)
                    }

                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(filteredMergePersons) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mergeTargetId = p.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = mergeTargetId == p.id,
                                    onClick = { mergeTargetId = p.id }
                                )
                                Text(p.fullName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (mergeTargetId != null) {
                        viewModel.mergePersons(sourceId = personToEdit!!.id, targetId = mergeTargetId!!, reason = "Manual merge triggered by user") { success ->
                            if (success) {
                                showMergeDialog = false
                                showAddDialog = false
                                personToEdit = null
                            } else {
                                errorMsg = "فشل الدمج"
                            }
                        }
                    }
                }) {
                    Text("دمج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAddFamilyDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFamilyDialog = false
                newFamilyName = ""
            },
            title = { Text("إضافة عائلة جديدة") },
            text = {
                OutlinedTextField(
                    value = newFamilyName,
                    onValueChange = { newFamilyName = it },
                    label = { Text("اسم العائلة") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFamilyName.isNotBlank()) {
                            viewModel.createAndSelectFamily(newFamilyName) { newId ->
                                selectedFamilyId = newId
                            }
                            showAddFamilyDialog = false
                            newFamilyName = ""
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddFamilyDialog = false
                    newFamilyName = ""
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            val searchQuery by viewModel.searchQuery.collectAsState()
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text("بحث عن شخص...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(persons) { person ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = person.fullName, style = MaterialTheme.typography.titleMedium)
                                Text(text = "رقم الهوية: ${person.publicCode}", style = MaterialTheme.typography.bodyMedium)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {
                                        fullName = person.fullName
                                        fatherName = person.fatherName ?: ""
                                        birthDateString = person.birthDate ?: ""
                                        selectedFamilyId = person.familyId
                                        selectedHouseId = person.houseId
                                        personToEdit = person
                                    }) {
                                        Text("تعديل")
                                    }
                                    TextButton(onClick = { onNavigateToCertificates(person.id, person.fullName) }) {
                                        Text("تأييد سكن")
                                    }
                                    TextButton(onClick = { onNavigateToAttachments("person", person.id) }) {
                                        Text("مرفقات")
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
}
