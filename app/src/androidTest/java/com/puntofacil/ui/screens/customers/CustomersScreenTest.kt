package com.puntofacil.ui.screens.customers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.puntofacil.MainActivity 
import com.puntofacil.ui.theme.PuntoFacilTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class CustomersScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Set the content to CustomersScreen.
        // The CustomersViewModel will be automatically provided by Hilt.
        composeTestRule.setContent {
            PuntoFacilTheme {
                CustomersScreen(
                    onNavigateBack = {} 
                    // onNavigateToCreateCustomer and onNavigateToCustomerDetail 
                    // are not params of the actual CustomersScreen composable
                )
            }
        }
    }

    @Test
    fun screenTitle_isDisplayed() {
        // Check if the TopAppBar title "Customers" is displayed.
        composeTestRule.onNodeWithText("Customers").assertIsDisplayed()
    }
}
