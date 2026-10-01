package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecycleBinScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockTransaction = Transaction(
        id = 1, userId = 1, accountId = 1, amount = 5000L, currency = Currency.AED,
        categoryId = 1, type = TransactionType.EXPENSE, merchant = null,
        description = "Coffee", notes = null, transactionDate = 0L
    )

    @Test
    fun emptyStateIsShownWhenNoDeletedTransactions() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RecycleBinScreen(deletedTransactions = emptyList(), onRestoreClick = {}, onClearAllClick = {}, onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText("Recycle bin is empty").assertExists()
        composeTestRule.onNodeWithContentDescription("Clear All").assertDoesNotExist()
    }

    @Test
    fun restoreButtonInvokesOnRestoreClickWithTheTransaction() {
        var restoredTransaction: Transaction? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RecycleBinScreen(
                    deletedTransactions = listOf(mockTransaction),
                    onRestoreClick = { restoredTransaction = it },
                    onClearAllClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Restore").performClick()

        assertEquals(mockTransaction, restoredTransaction)
    }

    @Test
    fun clearAllConfirmationInvokesOnClearAllClick() {
        var clearAllClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RecycleBinScreen(
                    deletedTransactions = listOf(mockTransaction),
                    onRestoreClick = {},
                    onClearAllClick = { clearAllClicked = true },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear All").performClick()
        composeTestRule.onNodeWithText("Clear All").performClick()

        assertTrue(clearAllClicked)
    }

    @Test
    fun backButtonInvokesOnBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                RecycleBinScreen(deletedTransactions = emptyList(), onRestoreClick = {}, onClearAllClick = {}, onBackClick = { backClicked = true })
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }
}
