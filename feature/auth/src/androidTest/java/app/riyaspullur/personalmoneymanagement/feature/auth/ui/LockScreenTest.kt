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
class LockScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun biometricButtonShownAndInvokesCallbackWhenAvailable() {
        var biometricClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LockScreen(
                    onUnlockClick = {},
                    onBiometricClick = { biometricClicked = true },
                    canUseBiometrics = true
                )
            }
        }

        composeTestRule.onNodeWithText("Unlock with Biometrics").performClick()

        assertTrue(biometricClicked)
    }

    @Test
    fun biometricButtonHiddenWhenUnavailable() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LockScreen(
                    onUnlockClick = {},
                    onBiometricClick = {},
                    canUseBiometrics = false
                )
            }
        }

        composeTestRule.onNodeWithText("Unlock with Biometrics").assertDoesNotExist()
    }

    @Test
    fun unlockButtonInvokesCallbackWithEnteredPassword() {
        var capturedPassword: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LockScreen(
                    onUnlockClick = { capturedPassword = it },
                    onBiometricClick = {},
                    canUseBiometrics = false
                )
            }
        }

        composeTestRule.onNodeWithText("Password").performTextInput("mypassword")
        composeTestRule.onNodeWithText("Unlock").performClick()

        assertEquals("mypassword", capturedPassword)
    }

    @Test
    fun errorMessageIsDisplayedWhenProvided() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LockScreen(
                    onUnlockClick = {},
                    onBiometricClick = {},
                    canUseBiometrics = false,
                    error = "Invalid password"
                )
            }
        }

        composeTestRule.onNodeWithText("Invalid password").assertExists()
    }
}
