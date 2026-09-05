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
    val regions by viewModel.regions.collectAsState()
    val streets by viewModel.streets.collectAsState()
    val alleys by viewModel.alleys.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var houseToEdit by remember { mutableStateOf<HouseEntity?>(null) }
    var houseNumber by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("occupied") }
    var selectedRegionId by remember { mutableStateOf<Long?>(null) }
    var selectedStreetId by remember { mutableStateOf<Long?>(null) }
    var selectedAlleyId by remember { mutableStateOf<Long?>(null) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    var expandedRegion by remember { mutableStateOf(false) }
    var expandedStreet by remember { mutableStateOf(false) }
    var expandedAlley by remember { mutableStateOf(false) }

    if (showAddDialog || houseToEdit != null) {
        val isEdit = houseToEdit != null

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                houseToEdit = null
                houseNumber = ""
                status = "occupied"
                selectedRegionId = null
                selectedStreetId = null
                selectedAlleyId = null
                errorMsg = null
                viewModel.clearDependentSelections()
            },
            title = { Text(if (isEdit) "تعديل الدار" else "إضافة دار جديدة") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expandedRegion,
                        onExpandedChange = { expandedRegion = !expandedRegion }
                    ) {
                        val selectedRegionName = regions.find { it.id == selectedRegionId }?.name ?: "اختر المنطقة"
                        OutlinedTextField(
                            value = selectedRegionName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المنطقة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegion) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRegion,
                            onDismissRequest = { expandedRegion = false }
                        ) {
                            regions.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region.name) },
                                    onClick = {
                                        selectedRegionId = region.id
                                        selectedStreetId = null
                                        selectedAlleyId = null
                                        expandedRegion = false
                                        viewModel.loadStreetsForRegion(region.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedStreet,
                        onExpandedChange = { if (selectedRegionId != null) expandedStreet = !expandedStreet }
                    ) {
                        val selectedStreetName = streets.find { it.id == selectedStreetId }?.name ?: "اختر الشارع"
                        OutlinedTextField(
                            value = selectedStreetName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الشارع") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStreet) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = selectedRegionId != null
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStreet,
                            onDismissRequest = { expandedStreet = false }
                        ) {
                            streets.forEach { street ->
                                DropdownMenuItem(
                                    text = { Text(street.name) },
                                    onClick = {
                                        selectedStreetId = street.id
                                        selectedAlleyId = null
                                        expandedStreet = false
                                        viewModel.loadAlleysForStreet(street.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedAlley,
                        onExpandedChange = { if (selectedStreetId != null) expandedAlley = !expandedAlley }
                    ) {
                        val selectedAlleyName = alleys.find { it.id == selectedAlleyId }?.name ?: "اختر الزقاق"
                        OutlinedTextField(
                            value = selectedAlleyName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الزقاق") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlley) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = selectedStreetId != null
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAlley,
                            onDismissRequest = { expandedAlley = false }
                        ) {
                            alleys.forEach { alley ->
                                DropdownMenuItem(
                                    text = { Text(alley.name) },
                                    onClick = {
                                        selectedAlleyId = alley.id
                                        expandedAlley = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                    if (houseNumber.isNotBlank() && status.isNotBlank()) {
                        val houseToSave = if (isEdit) {
                            houseToEdit!!.copy(
                                houseNumber = houseNumber,
                                status = status,
                                streetId = selectedStreetId,
                                alleyId = selectedAlleyId,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            HouseEntity(
                                publicCode = houseNumber,
                                internalNumber = houseNumber,
                                houseNumber = houseNumber,
                                streetId = selectedStreetId,
                                alleyId = selectedAlleyId,
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

                        viewModel.validateAndSaveHouse(houseToSave) { success, error ->
                            if (success) {
                                showAddDialog = false
                                houseToEdit = null
                                houseNumber = ""
                                status = "occupied"
                                selectedRegionId = null
                                selectedStreetId = null
                                selectedAlleyId = null
                                errorMsg = null
                                viewModel.clearDependentSelections()
                            } else {
                                errorMsg = error
                            }
                        }
                    } else {
                        errorMsg = "يرجى تعبئة جميع الحقول المطلوبة"
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
                    selectedRegionId = null
                    selectedStreetId = null
                    selectedAlleyId = null
                    errorMsg = null
                    viewModel.clearDependentSelections()
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
                                    selectedStreetId = house.streetId
                                    selectedAlleyId = house.alleyId
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
