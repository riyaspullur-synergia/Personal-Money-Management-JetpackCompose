package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddAccountScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun saveButtonDisabledUntilNameAndBalanceAreEntered() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddAccountScreen(groups = emptyList(), userId = 1L, onSaveClick = {}, onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.add_account_save_button)).assertIsNotEnabled()
    }

    @Test
    fun savingDefaultCashAccountInvokesOnSaveClickWithCorrectFields() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var capturedAccount: AccountRecord? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddAccountScreen(
                    groups = emptyList(),
                    userId = 42L,
                    onSaveClick = { capturedAccount = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.add_account_name_label)).performTextInput("Cash Wallet")
        composeTestRule.onNodeWithText(context.getString(R.string.add_account_initial_balance_label)).performTextInput("500")
        composeTestRule.onNodeWithText(context.getString(R.string.add_account_save_button)).performScrollTo().performClick()

        val account = requireNotNull(capturedAccount)
        assertEquals(42L, account.userId)
        assertEquals("Cash Wallet", account.name)
        assertEquals(AccountType.CASH, account.type)
        assertEquals(50000L, account.initialBalance)
        assertEquals(Currency.AED, account.currency)
        assertNull(account.groupId)
        assertNull(account.investedAmount)
        assertNull(account.isReceivable)
    }

    @Test
    fun selectingInvestmentTypeShowsInvestedAmountField() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddAccountScreen(groups = emptyList(), userId = 1L, onSaveClick = {}, onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.account_type_investment)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.add_account_invested_amount_label)).assertExists()
    }

    @Test
    fun selectingAccountGroupIncludesItInSavedAccount() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var capturedAccount: AccountRecord? = null
        val groups = listOf(AccountGroup(id = 5, userId = 1, name = "Banks"))

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AddAccountScreen(
                    groups = groups,
                    userId = 1L,
                    onSaveClick = { capturedAccount = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.add_account_name_label)).performTextInput("HDFC")
        composeTestRule.onNodeWithText(context.getString(R.string.add_account_initial_balance_label)).performTextInput("1000")
        composeTestRule.onNodeWithText("Banks").performScrollTo().performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.add_account_save_button)).performScrollTo().performClick()

        assertEquals(5L, requireNotNull(capturedAccount).groupId)
    }
}
