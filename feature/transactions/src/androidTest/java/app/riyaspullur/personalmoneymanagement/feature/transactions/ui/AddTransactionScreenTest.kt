package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddTransactionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAccounts = listOf(
        AccountRecord(id = 1, userId = 1, name = "Cash", type = AccountType.CASH, initialBalance = 100000, currency = Currency.AED, icon = null, color = 0xFF4CAF50.toInt()),
        AccountRecord(id = 2, userId = 1, name = "Bank", type = AccountType.BANK, initialBalance = 500000, currency = Currency.AED, icon = null, color = 0xFF2196F3.toInt())
    )

    private val mockCategories = listOf(
        Category(id = 10, userId = 1, name = "Food", icon = null, color = 0xFFF44336.toInt(), type = TransactionType.EXPENSE)
    )

    @Test
    fun noAccountsShowsMessageAndGoBackInvokesCallback() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var backClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddTransactionScreen(
                    accounts = emptyList(),
                    categories = emptyList(),
                    uiState = AddTransactionUiState.Idle,
                    onAddClick = { _, _, _, _, _, _, _, _, _ -> },
                    onAddCategoryClick = { _, _ -> },
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.common_no_accounts_found)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.common_go_back)).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun savingExpenseInvokesOnAddClickWithSelectedAccountAndCategory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var capturedAmount: Long? = null
        var capturedType: TransactionType? = null
        var capturedCategoryId: Long? = null
        var capturedAccountId: Long? = null
        var capturedToAccountId: Long? = null
        var capturedStatus: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddTransactionScreen(
                    accounts = mockAccounts,
                    categories = mockCategories,
                    uiState = AddTransactionUiState.Idle,
                    onAddClick = { amt, type, catId, accId, toAccId, _, status, _, _ ->
                        capturedAmount = amt
                        capturedType = type
                        capturedCategoryId = catId
                        capturedAccountId = accId
                        capturedToAccountId = toAccId
                        capturedStatus = status
                    },
                    onAddCategoryClick = { _, _ -> },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Cash").performClick()
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.add_transaction_amount_hint)).performTextInput("100")
        
        val saveButtonLabel = context.getString(R.string.add_transaction_save_action, context.getString(R.string.common_expense))
        composeTestRule.onNodeWithText(saveButtonLabel).performScrollTo().performClick()

        assertEquals(10000L, capturedAmount)
        assertEquals(TransactionType.EXPENSE, capturedType)
        assertEquals(10L, capturedCategoryId)
        assertEquals(1L, capturedAccountId)
        assertNull(capturedToAccountId)
        assertEquals("CLEARED", capturedStatus)
    }

    @Test
    fun addCategoryDialogInvokesOnAddCategoryClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var capturedName: String? = null
        var capturedType: TransactionType? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddTransactionScreen(
                    accounts = mockAccounts,
                    categories = emptyList(),
                    uiState = AddTransactionUiState.Idle,
                    onAddClick = { _, _, _, _, _, _, _, _, _ -> },
                    onAddCategoryClick = { name, type -> capturedName = name; capturedType = type },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.common_new)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.category_name_label)).performTextInput("Utilities")
        composeTestRule.onNodeWithText(context.getString(R.string.common_add)).performClick()

        assertEquals("Utilities", capturedName)
        assertEquals(TransactionType.EXPENSE, capturedType)
    }

    @Test
    fun errorMessageIsDisplayedWhenProvided() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddTransactionScreen(
                    accounts = mockAccounts,
                    categories = mockCategories,
                    uiState = AddTransactionUiState.Error(context.getString(R.string.error_failed_to_save_transaction)),
                    onAddClick = { _, _, _, _, _, _, _, _, _ -> },
                    onAddCategoryClick = { _, _ -> },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.error_failed_to_save_transaction)).assertExists()
    }
}
