package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelpSupportScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appNameAndVersionAreDisplayed() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                HelpSupportScreen(onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText("Personal Money Management").assertExists()
        composeTestRule.onNodeWithText("Version 1.0").assertExists()
    }

    @Test
    fun backButtonInvokesOnBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                HelpSupportScreen(onBackClick = { backClicked = true })
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }
}
