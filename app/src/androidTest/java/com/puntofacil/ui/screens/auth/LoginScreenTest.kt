package com.puntofacil.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.puntofacil.MainActivity // Using MainActivity to host the composable
import com.puntofacil.ui.theme.PuntoFacilTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Using MainActivity as the host for the composable.
    // Hilt will provide the ViewModel to LoginScreen.
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Set the content to LoginScreen.
        // The LoginViewModel will be automatically provided by Hilt.
        composeTestRule.setContent {
            PuntoFacilTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }
    }

    @Test
    fun emailAndPasswordFields_areDisplayed() {
        // Check if the outlined text field with label "Usuario" is displayed.
        // This targets the label, but implies the field is also displayed.
        composeTestRule.onNodeWithText("Usuario").assertIsDisplayed()
        
        // Check if the outlined text field with label "Contraseña" is displayed.
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }
}
