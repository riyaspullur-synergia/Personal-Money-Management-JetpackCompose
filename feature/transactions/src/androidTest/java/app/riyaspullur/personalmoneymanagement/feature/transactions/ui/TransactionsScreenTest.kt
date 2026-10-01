package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockTransaction = Transaction(
        id = 1, userId = 1, accountId = 1, amount = 15000L, currency = Currency.AED,
        categoryId = 1, type = TransactionType.EXPENSE, merchant = "Starbucks",
        description = "Coffee", notes = null, transactionDate = 0L
    )

    @Test
    fun emptyStateIsShownWhenNoTransactions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                TransactionsScreen(
                    uiState = TransactionsUiState.Success(emptyList()),
                    onBackClick = {},
                    onDeleteClick = {},
                    onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.common_no_transactions)).assertExists()
    }

    @Test
    fun backButtonInvokesOnBackClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var backClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                TransactionsScreen(
                    uiState = TransactionsUiState.Success(emptyList()),
                    onBackClick = { backClicked = true },
                    onDeleteClick = {},
                    onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.common_back)).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun confirmingDeleteInvokesOnDeleteClickWithTheTransaction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var deletedTransaction: Transaction? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                TransactionsScreen(
                    uiState = TransactionsUiState.Success(listOf(mockTransaction)),
                    onBackClick = {},
                    onDeleteClick = { deletedTransaction = it },
                    onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.common_delete)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.transactions_delete_title)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.common_delete)).performClick()

        assertEquals(mockTransaction, deletedTransaction)
    }

    @Test
    fun cancellingDeleteDoesNotInvokeOnDeleteClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var deleteInvoked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                TransactionsScreen(
                    uiState = TransactionsUiState.Success(listOf(mockTransaction)),
                    onBackClick = {},
                    onDeleteClick = { deleteInvoked = true },
                    onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.common_delete)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.common_cancel)).performClick()

        assertFalse(deleteInvoked)
    }
}
