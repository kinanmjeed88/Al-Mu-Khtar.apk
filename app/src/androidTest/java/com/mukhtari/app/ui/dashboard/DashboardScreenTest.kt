package com.mukhtari.app.ui.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDashboardNavigation() {
        var navigatedToRegions = false
        
        composeTestRule.setContent {
            DashboardScreen(
                onNavigateToRegions = { navigatedToRegions = true },
                onNavigateToHouses = {},
                onNavigateToFamilies = {},
                onNavigateToPersons = {},
                onNavigateToTransactions = {},
                onNavigateToSettings = {}
            )
        }

        composeTestRule.onNodeWithText("لوحة التحكم").assertExists()

        val regionsCard = composeTestRule.onNodeWithText("المناطق والشوارع")
        regionsCard.assertExists()
        regionsCard.performClick()

        assertTrue(navigatedToRegions)
    }
}
