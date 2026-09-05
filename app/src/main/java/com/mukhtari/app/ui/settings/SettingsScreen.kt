package com.mukhtari.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecycleBin: () -> Unit = {},
    onNavigateToActivityLog: () -> Unit = {}
) {
    val context = LocalContext.current
    var crashLogContent by remember { mutableStateOf<String?>(null) }
    val logFile = File(context.cacheDir, "crash_log.txt")

    LaunchedEffect(Unit) {
        if (logFile.exists() && logFile.length() > 0) {
            crashLogContent = logFile.readText()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = onNavigateToRecycleBin,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("سلة المحذوفات")
            }

            Button(
                onClick = onNavigateToActivityLog,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("سجل النشاطات")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Crash Log Diagnostics Viewer",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (crashLogContent != null) {
                Button(
                    onClick = {
                        if (logFile.exists()) {
                            logFile.delete()
                        }
                        crashLogContent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Clear Crash Log")
                }

                SelectionContainer(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        text = crashLogContent!!,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No recent crashes detected.")
                }
            }
        }
    }
}
