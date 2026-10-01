package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransferScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAccounts = listOf(
        AccountRecord(
            id = 1,
            userId = 1,
            name = "Cash",
            type = AccountType.CASH,
            initialBalance = 100000,
            currency = Currency.AED,
            icon = null,
            color = 0xFF4CAF50.toInt()
        ),
        AccountRecord(
            id = 2,
            userId = 1,
            name = "Bank",
            type = AccountType.BANK,
            initialBalance = 500000,
            currency = Currency.AED,
            icon = null,
            color = 0xFF2196F3.toInt()
        )
    )

    @Test
    fun executeTransferButtonDisabledUntilAccountsAndAmountAreSet() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                TransferScreen(
                    accounts = mockAccounts,
                    onTransferClick = { _, _, _, _, _ -> },
                    onBackClick = {})
            }
        }

        composeTestRule.onNodeWithText("Execute Transfer").assertIsNotEnabled()
    }

    @Test
    fun executeTransferInvokesCallbackWithSelectedAccountsAndAmount() {
        var capturedFrom: Long? = null
        var capturedTo: Long? = null
        var capturedAmount: Long? = null
        var capturedCurrency: Currency? = null
        var capturedNote: String? = null

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                TransferScreen(
                    accounts = mockAccounts,
                    onTransferClick = { from, to, amt, currency, note ->
                        capturedFrom = from
                        capturedTo = to
                        capturedAmount = amt
                        capturedCurrency = currency
                        capturedNote = note
                    },
                    onBackClick = {}
                )
            }
        }

        // The From picker always lists every account (unfiltered); the To picker only excludes
        // whichever account is currently selected as the source. So each account name can match
        // twice - disambiguate by position: the From row renders first, the To row second.
        composeTestRule.onAllNodesWithText("Cash")[0].performClick()
        composeTestRule.onAllNodesWithText("Bank")[1].performClick()
        composeTestRule.onNodeWithText("Amount").performTextInput("500")
        composeTestRule.onNodeWithText("Note").performTextInput("Rent")
        composeTestRule.onNodeWithText("Execute Transfer").performClick()

        assertEquals(1L, capturedFrom)
        assertEquals(2L, capturedTo)
        assertEquals(500L, capturedAmount)
        assertEquals(Currency.AED, capturedCurrency)
        assertEquals("Rent", capturedNote)
    }
}
