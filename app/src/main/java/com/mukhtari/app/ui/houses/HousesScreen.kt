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

    var showAddRegionDialog by remember { mutableStateOf(false) }
    var newRegionName by remember { mutableStateOf("") }

    var showAddStreetDialog by remember { mutableStateOf(false) }
    var newStreetName by remember { mutableStateOf("") }

    var showAddAlleyDialog by remember { mutableStateOf(false) }
    var newAlleyName by remember { mutableStateOf("") }

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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedRegion,
                            onExpandedChange = { expandedRegion = !expandedRegion },
                            modifier = Modifier.weight(1f)
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
                        IconButton(onClick = { showAddRegionDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة منطقة")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedStreet,
                            onExpandedChange = { if (selectedRegionId != null) expandedStreet = !expandedStreet },
                            modifier = Modifier.weight(1f)
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
                        if (selectedRegionId != null) {
                            IconButton(onClick = { showAddStreetDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة شارع")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedAlley,
                            onExpandedChange = { if (selectedStreetId != null) expandedAlley = !expandedAlley },
                            modifier = Modifier.weight(1f)
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
                        if (selectedStreetId != null) {
                            IconButton(onClick = { showAddAlleyDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة زقاق")
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
                                publicCode = "HSE-" + java.util.UUID.randomUUID().toString().take(8).uppercase(),
                                internalNumber = "HSE-" + java.util.UUID.randomUUID().toString().take(8).uppercase(),
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

    if (showAddRegionDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddRegionDialog = false
                newRegionName = ""
            },
            title = { Text("إضافة منطقة جديدة") },
            text = {
                OutlinedTextField(
                    value = newRegionName,
                    onValueChange = { newRegionName = it },
                    label = { Text("اسم المنطقة") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newRegionName.isNotBlank()) {
                            viewModel.createAndSelectRegion(newRegionName) { newId ->
                                selectedRegionId = newId
                                selectedStreetId = null
                                selectedAlleyId = null
                                viewModel.loadStreetsForRegion(newId)
                            }
                            showAddRegionDialog = false
                            newRegionName = ""
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddRegionDialog = false
                    newRegionName = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAddStreetDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddStreetDialog = false
                newStreetName = ""
            },
            title = { Text("إضافة شارع جديد") },
            text = {
                OutlinedTextField(
                    value = newStreetName,
                    onValueChange = { newStreetName = it },
                    label = { Text("اسم الشارع") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newStreetName.isNotBlank() && selectedRegionId != null) {
                            viewModel.createAndSelectStreet(selectedRegionId!!, newStreetName) { newId ->
                                selectedStreetId = newId
                                selectedAlleyId = null
                                viewModel.loadAlleysForStreet(newId)
                            }
                            showAddStreetDialog = false
                            newStreetName = ""
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddStreetDialog = false
                    newStreetName = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAddAlleyDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddAlleyDialog = false
                newAlleyName = ""
            },
            title = { Text("إضافة زقاق جديد") },
            text = {
                OutlinedTextField(
                    value = newAlleyName,
                    onValueChange = { newAlleyName = it },
                    label = { Text("اسم الزقاق") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAlleyName.isNotBlank() && selectedStreetId != null) {
                            viewModel.createAndSelectAlley(selectedStreetId!!, newAlleyName) { newId ->
                                selectedAlleyId = newId
                            }
                            showAddAlleyDialog = false
                            newAlleyName = ""
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddAlleyDialog = false
                    newAlleyName = ""
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
