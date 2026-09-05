package com.mukhtari.app.ui.visitors

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
import com.mukhtari.app.data.local.entity.VisitorLogEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorsScreen(
    onNavigateBack: () -> Unit,
    viewModel: VisitorsViewModel = koinViewModel()
) {
    val visitors by viewModel.visitors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var visitorToEdit by remember { mutableStateOf<VisitorLogEntity?>(null) }
    var visitorName by remember { mutableStateOf("") }
    var visitReason by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    if (showAddDialog || visitorToEdit != null) {
        val isEdit = visitorToEdit != null
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                visitorToEdit = null
                visitorName = ""
                visitReason = ""
                visitDate = ""
                phone = ""
            },
            title = { Text(if (isEdit) "تعديل زائر" else "إضافة زائر جديد") },
            text = {
                Column {
                    OutlinedTextField(
                        value = visitorName,
                        onValueChange = { visitorName = it },
                        label = { Text("اسم الزائر") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = visitReason,
                        onValueChange = { visitReason = it },
                        label = { Text("سبب الزيارة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = visitDate,
                        onValueChange = { visitDate = it },
                        label = { Text("تاريخ الزيارة") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (visitorName.isNotBlank() && visitReason.isNotBlank() && visitDate.isNotBlank()) {
                        val visitorToSave = if (isEdit) {
                            visitorToEdit!!.copy(
                                visitorName = visitorName,
                                visitReason = visitReason,
                                visitDate = visitDate,
                                phone = phone
                            )
                        } else {
                            VisitorLogEntity(
                                visitorName = visitorName,
                                phone = phone,
                                visitReason = visitReason,
                                transactionType = null,
                                visitDate = visitDate,
                                visitTime = null,
                                result = null,
                                notes = null,
                                createdAt = System.currentTimeMillis()
                            )
                        }
                        viewModel.saveVisitor(visitorToSave)
                        showAddDialog = false
                        visitorToEdit = null
                        visitorName = ""
                        visitReason = ""
                        visitDate = ""
                        phone = ""
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    visitorToEdit = null
                    visitorName = ""
                    visitReason = ""
                    visitDate = ""
                    phone = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل الزوار") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة زائر")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (visitors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد زوار.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(visitors) { visitor ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = visitor.visitorName, style = MaterialTheme.typography.titleMedium)
                            Text(text = "سبب: ${visitor.visitReason} | تاريخ: ${visitor.visitDate}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    visitorName = visitor.visitorName
                                    visitReason = visitor.visitReason
                                    visitDate = visitor.visitDate
                                    phone = visitor.phone ?: ""
                                    visitorToEdit = visitor
                                }) {
                                    Text("تعديل")
                                }
                                TextButton(
                                    onClick = { viewModel.deleteVisitor(visitor.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("حذف")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
