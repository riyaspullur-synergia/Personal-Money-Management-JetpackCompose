package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockUser = User(id = 1, username = "riyas", passwordHash = "", displayName = "Riyas Pullur", createdAt = 0L)

    @Test
    fun loadingStateShowsLoadingIndicator() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ProfileScreen(uiState = ProfileUiState.Loading, onUpdateClick = {}, onChangePasswordClick = { _, _ -> }, onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText("Loading...").assertExists()
    }

    @Test
    fun errorStateShowsErrorMessage() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ProfileScreen(uiState = ProfileUiState.Error("Failed to load profile"), onUpdateClick = {}, onChangePasswordClick = { _, _ -> }, onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText("Failed to load profile").assertExists()
    }

    @Test
    fun successStateShowsUserDisplayNameAndUsername() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ProfileScreen(uiState = ProfileUiState.Success(mockUser), onUpdateClick = {}, onChangePasswordClick = { _, _ -> }, onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText("Riyas Pullur").assertExists()
        composeTestRule.onNodeWithText("riyas").assertExists()
    }

    @Test
    fun editingDisplayNameAndSavingInvokesOnUpdateClick() {
        var capturedName: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ProfileScreen(
                    uiState = ProfileUiState.Success(mockUser),
                    onUpdateClick = { capturedName = it },
                    onChangePasswordClick = { _, _ -> },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Edit Profile").performClick()
        composeTestRule.onNodeWithText("Display Name").performTextClearance()
        composeTestRule.onNodeWithText("Display Name").performTextInput("New Name")
        composeTestRule.onNodeWithText("Save").performClick()

        assertEquals("New Name", capturedName)
    }

    @Test
    fun changePasswordDialogInvokesOnChangePasswordClick() {
        var capturedOld: String? = null
        var capturedNew: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ProfileScreen(
                    uiState = ProfileUiState.Success(mockUser),
                    onUpdateClick = {},
                    onChangePasswordClick = { old, new -> capturedOld = old; capturedNew = new },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Change Password").performClick()
        composeTestRule.onNodeWithText("Current Password").performTextInput("oldPass")
        composeTestRule.onNodeWithText("New Password").performTextInput("newPass")
        composeTestRule.onNodeWithText("Confirm New Password").performTextInput("newPass")
        composeTestRule.onNodeWithText("Change").performClick()

        assertEquals("oldPass", capturedOld)
        assertEquals("newPass", capturedNew)
    }

    @Test
    fun backButtonInvokesOnBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ProfileScreen(uiState = ProfileUiState.Success(mockUser), onUpdateClick = {}, onChangePasswordClick = { _, _ -> }, onBackClick = { backClicked = true })
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }
}
