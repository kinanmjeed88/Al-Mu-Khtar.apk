package com.mukhtari.app.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    isPinSet: Boolean,
    onPinSet: (String) -> Unit,
    onPinVerified: (String) -> Unit,
    error: String?
) {
    var pin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isPinSet) "إدخال الرمز" else "إعداد الرمز") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isPinSet) "الرجاء إدخال رمز القفل" else "الرجاء تعيين رمز قفل جديد",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("الرمز السري (PIN)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (isPinSet) onPinVerified(pin) else onPinSet(pin)
            }) {
                Text("تأكيد")
            }
        }
    }
}
