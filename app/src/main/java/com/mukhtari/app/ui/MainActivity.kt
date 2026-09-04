package com.mukhtari.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.mukhtari.app.ui.navigation.MainNavGraph
import com.mukhtari.app.ui.theme.MukhtariTheme
import com.mukhtari.app.ui.security.AppLockScreen
import com.mukhtari.app.ui.security.SecurityViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MukhtariTheme {
                // Enforce RTL
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val securityViewModel: SecurityViewModel = koinViewModel()
                        val isLocked by securityViewModel.isLocked.collectAsState()
                        val isPinSet by securityViewModel.isPinSet.collectAsState()
                        val error by securityViewModel.error.collectAsState()

                        if (isLocked) {
                            AppLockScreen(
                                isPinSet = isPinSet,
                                onPinSet = { securityViewModel.setPin(it) },
                                onPinVerified = { securityViewModel.verifyPin(it) },
                                error = error
                            )
                        } else {
                            MainNavGraph()
                        }
                    }
                }
            }
        }
    }
}
