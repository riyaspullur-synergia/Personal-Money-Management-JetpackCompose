package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        isAppLockEnabled: Boolean = true,
        language: String = "en",
        onAppLockChange: (Boolean) -> Unit = {},
        onThemeChange: (String) -> Unit = {},
        onLanguageChange: (String) -> Unit = {},
        onRecycleBinClick: () -> Unit = {},
        onProfileClick: () -> Unit = {},
        onHelpSupportClick: () -> Unit = {},
        onLogoutClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                SettingsScreen(
                    isAppLockEnabled = isAppLockEnabled,
                    themeMode = "SYSTEM",
                    language = language,
                    onAppLockChange = onAppLockChange,
                    onThemeChange = onThemeChange,
                    onLanguageChange = onLanguageChange,
                    onRecycleBinClick = onRecycleBinClick,
                    onProfileClick = onProfileClick,
                    onHelpSupportClick = onHelpSupportClick,
                    onLogoutClick = onLogoutClick
                )
            }
        }
    }

    @Test
    fun toggleAppLockSwitchInvokesOnAppLockChangeWithOppositeValue() {
        var capturedValue: Boolean? = null
        setContent(isAppLockEnabled = true, onAppLockChange = { capturedValue = it })

        composeTestRule.onNode(isToggleable()).performClick()

        assertFalse(requireNotNull(capturedValue))
    }

    @Test
    fun selectingThemeInvokesOnThemeChange() {
        var capturedTheme: String? = null
        setContent(onThemeChange = { capturedTheme = it })

        composeTestRule.onNodeWithText("Theme").performClick()
        composeTestRule.onNodeWithText("Dark").performClick()

        assertEquals("DARK", capturedTheme)
    }

    @Test
    fun selectingLanguageInvokesOnLanguageChange() {
        var capturedLanguage: String? = null
        setContent(onLanguageChange = { capturedLanguage = it })

        composeTestRule.onNodeWithText("Language").performClick()
        composeTestRule.onNodeWithText("മലയാളം").performClick()

        assertEquals("ml", capturedLanguage)
    }

    @Test
    fun recycleBinClickInvokesCallback() {
        var clicked = false
        setContent(onRecycleBinClick = { clicked = true })

        composeTestRule.onNodeWithText("Recycle Bin").performClick()

        assertTrue(clicked)
    }

    @Test
    fun profileClickInvokesCallback() {
        var clicked = false
        setContent(onProfileClick = { clicked = true })

        composeTestRule.onNodeWithText("Profile").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun helpSupportClickInvokesCallback() {
        var clicked = false
        setContent(onHelpSupportClick = { clicked = true })

        composeTestRule.onNodeWithText("Help & Support").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun logoutClickShowsConfirmationDialog() {
        setContent()

        composeTestRule.onNodeWithText("Logout").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Are you sure you want to log out?").assertExists()
    }
}
