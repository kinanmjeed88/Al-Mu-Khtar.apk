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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mukhtari.app.data.local.entity.RegionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegionDetail: (Long?) -> Unit
) {
    val regions = listOf(
        RegionEntity(1, "REG-01", "Baghdad", "Karkh", "Mansour", "601", "Al-Mansour", null, null, 0, null, null, 0, 0)
    )

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
            FloatingActionButton(onClick = { onNavigateToRegionDetail(null) }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منطقة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(regions) { region ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToRegionDetail(region.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = region.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "\${region.governorate} - \${region.district}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
