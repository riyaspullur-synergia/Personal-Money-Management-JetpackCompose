package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.EmptyState
import app.riyaspullur.personalmoneymanagement.core.ui.components.LoadingScreen
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

/**
 * The ViewModel keeps a fixed English fallback literal ("Unknown error", unchanged, to stay
 * backward compatible with existing ViewModel tests). This maps it to a localized string at
 * the UI boundary; any other message (a real exception's text) is shown as-is since it isn't
 * designed, translatable UI copy.
 */
@Composable
private fun localizedAccountsError(message: String): String = when (message) {
    "Unknown error" -> stringResource(R.string.common_unknown_error)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    uiState: AccountsUiState,
    onAddAccountClick: () -> Unit,
    onAddGroupClick: (String) -> Unit
) {
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }

    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text(stringResource(R.string.accounts_add_group_title)) },
            text = {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text(stringResource(R.string.accounts_group_name_label)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (groupName.isNotBlank()) {
                        onAddGroupClick(groupName)
                        groupName = ""
                        showAddGroupDialog = false
                    }
                }) {
                    Text(stringResource(R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.accounts)) },
                actions = {
                    IconButton(onClick = { showAddGroupDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.accounts_add_group_content_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccountClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.accounts_add_account_content_description))
            }
        }
    ) { padding ->
        when (uiState) {
            is AccountsUiState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
            is AccountsUiState.Success -> {
                if (uiState.accounts.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = stringResource(R.string.accounts_empty_title),
                        subtitle = stringResource(R.string.accounts_empty_subtitle),
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    AccountsContent(
                        accounts = uiState.accounts,
                        groups = uiState.groups,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    )
                }
            }
            is AccountsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = localizedAccountsError(uiState.message), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AccountsContent(
    accounts: List<Account>,
    groups: List<AccountGroup>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unmapped accounts
        val ungroupedAccounts = accounts.filter { it.groupId == null }
        if (ungroupedAccounts.isNotEmpty()) {
            item {
                Text(text = stringResource(R.string.accounts_general_section_title), style = MaterialTheme.typography.titleMedium)
            }
            items(ungroupedAccounts) { account ->
                AccountItem(account)
            }
        }

        // Grouped accounts
        groups.forEach { group ->
            val groupAccounts = accounts.filter { it.groupId == group.id }
            if (groupAccounts.isNotEmpty()) {
                item {
                    Text(text = group.name, style = MaterialTheme.typography.titleMedium)
                }
                items(groupAccounts) { account ->
                    AccountItem(account)
                }
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun AccountsScreenPreview() {
    val mockAccounts = listOf(
        Account(
            id = 1,
            userId = 1,
            groupId = null,
            name = "Cash",
            type = AccountType.CASH,
            currency = Currency.AED,
            color = 0xFF4CAF50.toInt(),
            currentBalance = Money(100000L, Currency.AED)
        ),
        Account(
            id = 2,
            userId = 1,
            groupId = 1,
            name = "HDFC Bank",
            type = AccountType.BANK,
            currency = Currency.INR,
            color = 0xFF2196F3.toInt(),
            currentBalance = Money(500000L, Currency.INR)
        )
    )
    val mockGroups = listOf(
        AccountGroup(id = 1, userId = 1, name = "Primary Banks")
    )

    PersonalMoneyManagemntTheme {
        AccountsScreen(
            uiState = AccountsUiState.Success(mockAccounts, mockGroups),
            onAddAccountClick = {},
            onAddGroupClick = {}
        )
    }
}
