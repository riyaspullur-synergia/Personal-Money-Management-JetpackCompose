package app.riyaspullur.personalmoneymanagement.feature.reports.ui

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
class PdfViewerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun missingFileShowsErrorMessage() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                PdfViewerScreen(
                    filePath = "/nonexistent/path/report.pdf",
                    onBackClick = {},
                    onShareClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("File not found").assertExists()
    }

    @Test
    fun backButtonInvokesOnBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                PdfViewerScreen(
                    filePath = "/nonexistent/path/report.pdf",
                    onBackClick = { backClicked = true },
                    onShareClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }

    @Test
    fun shareButtonInvokesOnShareClick() {
        var shareClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                PdfViewerScreen(
                    filePath = "/nonexistent/path/report.pdf",
                    onBackClick = {},
                    onShareClick = { shareClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share").performClick()

        assertTrue(shareClicked)
    }
}
