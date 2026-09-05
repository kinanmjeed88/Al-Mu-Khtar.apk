package com.mukhtari.app.ui.regions

import androidx.compose.foundation.clickable
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
import com.mukhtari.app.data.local.entity.RegionEntity
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegionsViewModel = koinViewModel()
) {
    val regions by viewModel.regions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var regionToEdit by remember { mutableStateOf<RegionEntity?>(null) }

    var regionName by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }

    if (showAddDialog || regionToEdit != null) {
        val isEdit = regionToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                regionToEdit = null
                regionName = ""
                governorate = ""
                district = ""
            },
            title = { Text(if (isEdit) "تعديل منطقة" else "إضافة منطقة جديدة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = regionName,
                        onValueChange = { regionName = it },
                        label = { Text("اسم المنطقة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = governorate,
                        onValueChange = { governorate = it },
                        label = { Text("المحافظة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("القضاء") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (regionName.isNotBlank() && governorate.isNotBlank() && district.isNotBlank()) {
                        val regionToSave = if (isEdit) {
                            regionToEdit!!.copy(
                                governorate = governorate,
                                district = district,
                                name = regionName,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            RegionEntity(
                                publicCode = java.util.UUID.randomUUID().toString().take(8).uppercase(),
                                governorate = governorate,
                                district = district,
                                subDistrict = "",
                                mahalla = "",
                                name = regionName,
                                description = null,
                                notes = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = null,
                                deletedReason = null
                            )
                        }
                        viewModel.saveRegion(regionToSave)
                        showAddDialog = false
                        regionToEdit = null
                        regionName = ""
                        governorate = ""
                        district = ""
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    regionToEdit = null
                    regionName = ""
                    governorate = ""
                    district = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المناطق") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منطقة")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (regions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد مناطق مضافة.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(regions) { region ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = region.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${region.governorate} - ${region.district}", style = MaterialTheme.typography.bodyMedium)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    regionName = region.name
                                    governorate = region.governorate
                                    district = region.district
                                    regionToEdit = region
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(onClick = { viewModel.deleteRegion(region.id) }) {
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
