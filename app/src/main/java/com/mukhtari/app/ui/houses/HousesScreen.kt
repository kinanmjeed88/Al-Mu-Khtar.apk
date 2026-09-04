package com.mukhtari.app.ui.houses

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
import com.mukhtari.app.data.local.entity.HouseEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousesScreen(
    onNavigateBack: () -> Unit,
    viewModel: HousesViewModel = koinViewModel()
) {
    val houses by viewModel.houses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var houseToEdit by remember { mutableStateOf<HouseEntity?>(null) }
    var houseNumber by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("occupied") }

    if (showAddDialog || houseToEdit != null) {
        val isEdit = houseToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                houseToEdit = null
                houseNumber = ""
                status = "occupied"
            },
            title = { Text(if (isEdit) "تعديل الدار" else "إضافة دار جديدة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = houseNumber,
                        onValueChange = { houseNumber = it },
                        label = { Text("رقم الدار") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("الحالة (occupied, vacant, etc)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (houseNumber.isNotBlank() && status.isNotBlank()) {
                        val houseToSave = if (isEdit) {
                            houseToEdit!!.copy(
                                houseNumber = houseNumber,
                                status = status,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            HouseEntity(
                                publicCode = houseNumber,
                                internalNumber = houseNumber,
                                houseNumber = houseNumber,
                                streetId = null,
                                alleyId = null,
                                mahallaNumber = null,
                                detailedAddress = null,
                                photoPath = null,
                                propertyType = "owned",
                                status = status,
                                ownershipType = "owned",
                                ownerName = null,
                                ownerPhone = null,
                                notes = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = null,
                                deletedReason = null
                            )
                        }
                        viewModel.saveHouse(houseToSave)
                        showAddDialog = false
                        houseToEdit = null
                        houseNumber = ""
                        status = "occupied"
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    houseToEdit = null
                    houseNumber = ""
                    status = "occupied"
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الدور") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة دار")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (houses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد دور مضافة.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(houses) { house ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "دار رقم: ${house.houseNumber}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "الحالة: ${house.status}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    houseNumber = house.houseNumber
                                    status = house.status
                                    houseToEdit = house
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(onClick = { viewModel.deleteHouse(house.id) }) {
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
