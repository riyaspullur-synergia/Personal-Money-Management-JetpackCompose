package app.riyaspullur.personalmoneymanagement.feature.auth.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun createAccountInvokesCallbackWithEnteredFields() {
        var capturedUsername: String? = null
        var capturedPassword: String? = null
        var capturedDisplayName: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RegisterScreen(
                    onRegisterClick = { u, p, d ->
                        capturedUsername = u
                        capturedPassword = p
                        capturedDisplayName = d
                    },
                    onLoginClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Display Name").performTextInput("Riyas Pullur")
        composeTestRule.onNodeWithText("Username").performTextInput("riyas")
        composeTestRule.onNodeWithText("Password").performTextInput("secret123")
        composeTestRule.onNodeWithText("Create Account").performClick()

        assertEquals("riyas", capturedUsername)
        assertEquals("secret123", capturedPassword)
        assertEquals("Riyas Pullur", capturedDisplayName)
    }

    @Test
    fun loginLinkInvokesOnLoginClick() {
        var loginClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RegisterScreen(
                    onRegisterClick = { _, _, _ -> },
                    onLoginClick = { loginClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Already have an account? Login").performClick()

        assertTrue(loginClicked)
    }

    @Test
    fun errorMessageIsDisplayedWhenProvided() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RegisterScreen(
                    onRegisterClick = { _, _, _ -> },
                    onLoginClick = {},
                    error = "Registration failed: username taken"
                )
            }
        }

        composeTestRule.onNodeWithText("Registration failed: username taken").assertExists()
    }
}
