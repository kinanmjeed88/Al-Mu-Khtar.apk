package com.mukhtari.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mukhtari.app.ui.dashboard.DashboardScreen
import com.mukhtari.app.ui.regions.RegionsScreen

@Composable
fun MainNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToRegions = { navController.navigate("regions") },
                onNavigateToHouses = { navController.navigate("houses") },
                onNavigateToFamilies = { navController.navigate("families") },
                onNavigateToPersons = { navController.navigate("persons") },
                onNavigateToTransactions = { navController.navigate("transactions") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("regions") {
            RegionsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("houses") {
            // Placeholder
        }
        composable("families") {
            // Placeholder
        }
        composable("persons") {
            // Placeholder
        }
        composable("transactions") {
            // Placeholder
        }
        composable("settings") {
            // Placeholder
        }
    }
}
