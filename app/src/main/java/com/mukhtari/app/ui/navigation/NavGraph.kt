package com.mukhtari.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mukhtari.app.ui.dashboard.DashboardScreen
import com.mukhtari.app.ui.regions.RegionsScreen
import com.mukhtari.app.ui.families.FamiliesScreen
import com.mukhtari.app.ui.persons.PersonsScreen
import com.mukhtari.app.ui.houses.HousesScreen
import com.mukhtari.app.ui.transactions.TransactionsScreen
import com.mukhtari.app.ui.settings.SettingsScreen
import com.mukhtari.app.ui.visitors.VisitorsScreen
import com.mukhtari.app.ui.letters.IncomingLettersScreen
import com.mukhtari.app.ui.letters.OutgoingLettersScreen
import com.mukhtari.app.ui.certificates.ResidencyCertificateScreen
import com.mukhtari.app.ui.recyclebin.RecycleBinScreen
import com.mukhtari.app.ui.activitylog.ActivityLogScreen
import com.mukhtari.app.ui.attachments.AttachmentsScreen
import com.mukhtari.app.ui.backup.BackupRestoreScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
                onNavigateToVisitors = { navController.navigate("visitors") },
                onNavigateToIncomingLetters = { navController.navigate("incoming_letters") },
                onNavigateToOutgoingLetters = { navController.navigate("outgoing_letters") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("regions") {
            RegionsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("houses") {
            HousesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("families") {
            FamiliesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("persons") {
            PersonsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCertificates = { personId, personName ->
                    navController.navigate("certificates/$personId/$personName")
                },
                onNavigateToAttachments = { ownerType, ownerId ->
                    navController.navigate("attachments/$ownerType/$ownerId")
                }
            )
        }
        composable(
            route = "certificates/{personId}/{personName}",
            arguments = listOf(
                navArgument("personId") { type = NavType.LongType },
                navArgument("personName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: 0L
            val personName = backStackEntry.arguments?.getString("personName") ?: ""
            ResidencyCertificateScreen(
                personId = personId,
                personName = personName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("transactions") {
            TransactionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecycleBin = { navController.navigate("recycle_bin") },
                onNavigateToActivityLog = { navController.navigate("activity_log") },
                onNavigateToBackupRestore = { navController.navigate("backup_restore") }
            )
        }
        composable("visitors") {
            VisitorsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("backup_restore") {
            BackupRestoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("incoming_letters") {
            IncomingLettersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("outgoing_letters") {
            OutgoingLettersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("recycle_bin") {
            RecycleBinScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("activity_log") {
            ActivityLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "attachments/{ownerType}/{ownerId}",
            arguments = listOf(
                navArgument("ownerType") { type = NavType.StringType },
                navArgument("ownerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val ownerType = backStackEntry.arguments?.getString("ownerType") ?: ""
            val ownerId = backStackEntry.arguments?.getLong("ownerId") ?: 0L
            AttachmentsScreen(
                ownerType = ownerType,
                ownerId = ownerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
