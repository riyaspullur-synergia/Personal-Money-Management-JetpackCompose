package app.riyaspullur.personalmoneymanagement.feature.auth.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented: hosts the screen composable directly (not via MainActivity, which sets
 * FLAG_SECURE) so Compose UI interactions can be exercised without a full Hilt/Room graph.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockUsers = listOf(
        User(id = 1, username = "riyas", passwordHash = "", displayName = "Riyas Pullur"),
        User(id = 2, username = "guest", passwordHash = "", displayName = "Guest User")
    )

    @Test
    fun loginButtonInvokesCallbackWithSelectedUserAndPassword() {
        var loggedInUsername: String? = null
        var loggedInPassword: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LoginScreen(
                    users = mockUsers,
                    onLoginClick = { u, p -> loggedInUsername = u; loggedInPassword = p },
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Riyas Pullur").performClick()
        composeTestRule.onNodeWithText("Password").performTextInput("secret123")
        composeTestRule.onNodeWithText("Log In").performClick()

        assertEquals("riyas", loggedInUsername)
        assertEquals("secret123", loggedInPassword)
    }

    @Test
    fun registerLinkInvokesOnRegisterClick() {
        var registerClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LoginScreen(
                    users = emptyList(),
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { registerClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()

        assertTrue(registerClicked)
    }

    @Test
    fun errorMessageIsDisplayedWhenProvided() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LoginScreen(
                    users = emptyList(),
                    onLoginClick = { _, _ -> },
                    onRegisterClick = {},
                    error = "Invalid username or password"
                )
            }
        }

        composeTestRule.onNodeWithText("Invalid username or password").assertExists()
    }

    @Test
    fun noUserChipsShownWhenUsersListIsEmpty() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                LoginScreen(
                    users = emptyList(),
                    onLoginClick = { _, _ -> },
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Select User").assertDoesNotExist()
    }
}
