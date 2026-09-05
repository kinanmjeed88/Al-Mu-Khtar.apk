package com.mukhtari.app.ui.families

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
import com.mukhtari.app.data.local.entity.FamilyEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FamiliesViewModel = koinViewModel()
) {
    val families by viewModel.families.collectAsState()
    val houses by viewModel.houses.collectAsState()
    val familyPersons by viewModel.familyPersons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var familyToEdit by remember { mutableStateOf<FamilyEntity?>(null) }
    var familyCode by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var selectedHouseId by remember { mutableStateOf<Long?>(null) }
    var selectedHeadOfFamilyId by remember { mutableStateOf<Long?>(null) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showAddHouseDialog by remember { mutableStateOf(false) }
    var newHouseNumber by remember { mutableStateOf("") }

    var expandedHouse by remember { mutableStateOf(false) }
    var expandedHead by remember { mutableStateOf(false) }

    if (showAddDialog || familyToEdit != null) {
        val isEdit = familyToEdit != null

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                familyToEdit = null
                familyCode = ""
                familyName = ""
                selectedHouseId = null
                selectedHeadOfFamilyId = null
                errorMsg = null
                viewModel.clearFamilyPersons()
            },
            title = { Text(if (isEdit) "تعديل عائلة" else "إضافة عائلة جديدة") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedHouse,
                            onExpandedChange = { expandedHouse = !expandedHouse },
                            modifier = Modifier.weight(1f)
                        ) {
                            val selectedHouseNum = houses.find { it.id == selectedHouseId }?.houseNumber ?: "اختر الدار"
                            OutlinedTextField(
                                value = selectedHouseNum,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الدار") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHouse) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHouse,
                                onDismissRequest = { expandedHouse = false }
                            ) {
                                houses.forEach { house ->
                                    DropdownMenuItem(
                                        text = { Text("دار رقم ${house.houseNumber}") },
                                        onClick = {
                                            selectedHouseId = house.id
                                            expandedHouse = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { showAddHouseDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة دار")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEdit && familyPersons.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedHead,
                            onExpandedChange = { expandedHead = !expandedHead }
                        ) {
                            val selectedHeadName = familyPersons.find { it.id == selectedHeadOfFamilyId }?.fullName ?: "لا يوجد رب أسرة"
                            OutlinedTextField(
                                value = selectedHeadName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("رب الأسرة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHead) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHead,
                                onDismissRequest = { expandedHead = false }
                            ) {
                                familyPersons.forEach { person ->
                                    DropdownMenuItem(
                                        text = { Text(person.fullName) },
                                        onClick = {
                                            selectedHeadOfFamilyId = person.id
                                            expandedHead = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = familyCode,
                        onValueChange = { familyCode = it },
                        label = { Text("كود العائلة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("اسم العائلة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (familyCode.isNotBlank() && selectedHouseId != null) {
                        val familyToSave = if (isEdit) {
                            familyToEdit!!.copy(
                                familyCode = familyCode,
                                familyName = familyName,
                                houseId = selectedHouseId,
                                headOfFamilyId = selectedHeadOfFamilyId,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            FamilyEntity(
                                familyCode = familyCode,
                                publicCode = java.util.UUID.randomUUID().toString().take(8).uppercase(),
                                familyName = familyName,
                                houseId = selectedHouseId,
                                headOfFamilyId = null,
                                residencyDate = null,
                                residencyStatus = "resident",
                                infoSource = null,
                                notes = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = null,
                                deletedReason = null
                            )
                        }

                        viewModel.validateAndSaveFamily(familyToSave) { success, error ->
                            if (success) {
                                showAddDialog = false
                                familyToEdit = null
                                familyCode = ""
                                familyName = ""
                                selectedHouseId = null
                                selectedHeadOfFamilyId = null
                                errorMsg = null
                                viewModel.clearFamilyPersons()
                            } else {
                                errorMsg = error
                            }
                        }
                    } else {
                        errorMsg = "يرجى تعبئة جميع الحقول المطلوبة واختيار الدار"
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    familyToEdit = null
                    familyCode = ""
                    familyName = ""
                    selectedHouseId = null
                    selectedHeadOfFamilyId = null
                    errorMsg = null
                    viewModel.clearFamilyPersons()
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAddHouseDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddHouseDialog = false
                newHouseNumber = ""
            },
            title = { Text("إضافة دار جديدة") },
            text = {
                OutlinedTextField(
                    value = newHouseNumber,
                    onValueChange = { newHouseNumber = it },
                    label = { Text("رقم الدار") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHouseNumber.isNotBlank()) {
                            viewModel.createAndSelectHouse(newHouseNumber) { newId ->
                                selectedHouseId = newId
                            }
                            showAddHouseDialog = false
                            newHouseNumber = ""
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddHouseDialog = false
                    newHouseNumber = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("العوائل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عائلة")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (families.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد عوائل مضافة.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(families) { family ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = family.familyName ?: "غير محدد", style = MaterialTheme.typography.titleMedium)
                            Text(text = "كود العائلة: ${family.familyCode}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    familyCode = family.familyCode
                                    familyName = family.familyName ?: ""
                                    selectedHouseId = family.houseId
                                    selectedHeadOfFamilyId = family.headOfFamilyId
                                    familyToEdit = family
                                    viewModel.loadPersonsForFamily(family.id)
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(onClick = { viewModel.deleteFamily(family.id) }) {
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
