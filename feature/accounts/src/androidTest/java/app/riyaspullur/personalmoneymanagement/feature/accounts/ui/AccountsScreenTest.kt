package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAccount = Account(
        id = 1,
        userId = 1,
        groupId = null,
        name = "My Wallet",
        type = AccountType.CASH,
        currency = Currency.AED,
        color = 0xFF4CAF50.toInt(),
        currentBalance = Money(100000L, Currency.AED)
    )

    @Test
    fun loadingStateShowsLoadingIndicator() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AccountsScreen(
                    uiState = AccountsUiState.Loading,
                    onAddAccountClick = {},
                    onAddGroupClick = {})
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.common_loading)).assertExists()
    }

    @Test
    fun emptyStateIsShownWhenNoAccounts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AccountsScreen(
                    uiState = AccountsUiState.Success(emptyList(), emptyList()),
                    onAddAccountClick = {}, onAddGroupClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.accounts_empty_title)).assertExists()
    }

    @Test
    fun errorStateShowsErrorMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AccountsScreen(
                    uiState = AccountsUiState.Error(context.getString(R.string.error_failed_to_load_accounts)),
                    onAddAccountClick = {},
                    onAddGroupClick = {})
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.error_failed_to_load_accounts)).assertExists()
    }

    @Test
    fun successStateShowsAccountNameAndBalance() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AccountsScreen(
                    uiState = AccountsUiState.Success(listOf(mockAccount), emptyList()),
                    onAddAccountClick = {}, onAddGroupClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("My Wallet").assertExists()
    }

    @Test
    fun addAccountFabInvokesOnAddAccountClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var addAccountClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AccountsScreen(
                    uiState = AccountsUiState.Success(emptyList(), emptyList()),
                    onAddAccountClick = { addAccountClicked = true }, onAddGroupClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.accounts_add_account_content_description)).performClick()

        assertTrue(addAccountClicked)
    }

    @Test
    fun addGroupDialogInvokesOnAddGroupClickWithEnteredName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var capturedGroupName: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                AccountsScreen(
                    uiState = AccountsUiState.Success(emptyList(), emptyList()),
                    onAddAccountClick = {}, onAddGroupClick = { capturedGroupName = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.accounts_add_group_content_description)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.accounts_group_name_label)).performTextInput("Banks")
        composeTestRule.onNodeWithText(context.getString(R.string.common_add)).performClick()

        assertEquals("Banks", capturedGroupName)
    }
}
