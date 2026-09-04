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
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var familyToEdit by remember { mutableStateOf<FamilyEntity?>(null) }
    var familyCode by remember { mutableStateOf("") }
    var publicCode by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }

    if (showAddDialog || familyToEdit != null) {
        val isEdit = familyToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                familyToEdit = null
                familyCode = ""
                publicCode = ""
                familyName = ""
            },
            title = { Text(if (isEdit) "تعديل عائلة" else "إضافة عائلة جديدة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = familyCode,
                        onValueChange = { familyCode = it },
                        label = { Text("كود العائلة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = publicCode,
                        onValueChange = { publicCode = it },
                        label = { Text("الكود العام") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("اسم العائلة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (familyCode.isNotBlank() && publicCode.isNotBlank()) {
                        val familyToSave = if (isEdit) {
                            familyToEdit!!.copy(
                                familyCode = familyCode,
                                publicCode = publicCode,
                                familyName = familyName,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            FamilyEntity(
                                familyCode = familyCode,
                                publicCode = publicCode,
                                familyName = familyName,
                                houseId = null,
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
                        viewModel.saveFamily(familyToSave)
                        showAddDialog = false
                        familyToEdit = null
                        familyCode = ""
                        publicCode = ""
                        familyName = ""
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
                    publicCode = ""
                    familyName = ""
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
                                    publicCode = family.publicCode
                                    familyName = family.familyName ?: ""
                                    familyToEdit = family
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
