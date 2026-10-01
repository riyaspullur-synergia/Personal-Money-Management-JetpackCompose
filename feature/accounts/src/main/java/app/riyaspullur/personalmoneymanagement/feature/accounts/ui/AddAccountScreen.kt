package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.SectionCard
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel
import app.riyaspullur.personalmoneymanagement.core.util.Calculator
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAccountScreen(
    groups: List<AccountGroup>,
    userId: Long,
    onSaveClick: (AccountRecord) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.CASH) }
    var balanceExpr by remember { mutableStateOf("") }
    val calculatedBalance = remember(balanceExpr) { Calculator.evaluate(balanceExpr) }
    var currency by remember { mutableStateOf(Currency.AED) }

    // Investment fields
    var investedAmountExpr by remember { mutableStateOf("") }
    val calculatedInvestedAmount = remember(investedAmountExpr) { Calculator.evaluate(investedAmountExpr) }

    // Deposit fields
    var interestRate by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var maturityDate by remember { mutableStateOf<Long?>(null) }

    // Debt fields
    var personName by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }

    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    val colors = listOf(0xFF2196F3.toInt(), 0xFF4CAF50.toInt(), 0xFFF44336.toInt(), 0xFFFFC107.toInt(), 0xFF9C27B0.toInt(), 0xFF607D8B.toInt())
    var selectedColor by remember { mutableIntStateOf(colors[0]) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.add_account_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = stringResource(R.string.add_account_details_section_title)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.add_account_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = stringResource(R.string.add_account_type_label), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.displayLabel()) }
                        )
                    }
                }

                Text(text = stringResource(R.string.common_currency), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Currency.entries.forEach { c ->
                        FilterChip(
                            selected = currency == c,
                            onClick = { currency = c },
                            label = { Text(c.code) }
                        )
                    }
                }

                OutlinedTextField(
                    value = balanceExpr,
                    onValueChange = { balanceExpr = it },
                    label = {
                        Text(
                            if (type == AccountType.INVESTMENT || type == AccountType.GOLD) {
                                stringResource(R.string.add_account_current_value_label)
                            } else {
                                stringResource(R.string.add_account_initial_balance_label)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (calculatedBalance != null && balanceExpr.contains(Regex("[+\\-*/]"))) {
                            Text(text = stringResource(R.string.add_account_total_label, calculatedBalance.toString()), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )

                // Specialized Fields
                when (type) {
                    AccountType.INVESTMENT, AccountType.GOLD -> {
                        OutlinedTextField(
                            value = investedAmountExpr,
                            onValueChange = { investedAmountExpr = it },
                            label = { Text(stringResource(R.string.add_account_invested_amount_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    AccountType.FIXED_DEPOSIT, AccountType.RECURRING_DEPOSIT -> {
                        OutlinedTextField(
                            value = interestRate,
                            onValueChange = { interestRate = it },
                            label = { Text(stringResource(R.string.add_account_interest_rate_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text(stringResource(R.string.add_account_bank_name_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    AccountType.RECEIVABLE, AccountType.LIABILITY -> {
                        OutlinedTextField(
                            value = personName,
                            onValueChange = { personName = it },
                            label = {
                                Text(
                                    if (type == AccountType.RECEIVABLE) {
                                        stringResource(R.string.add_account_lent_to_label)
                                    } else {
                                        stringResource(R.string.add_account_owed_to_label)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {}
                }
            }

            SectionCard(title = stringResource(R.string.add_account_organization_section_title)) {
                Text(text = stringResource(R.string.add_account_group_label), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedGroupId == null,
                        onClick = { selectedGroupId = null },
                        label = { Text(stringResource(R.string.add_account_no_group_label)) }
                    )
                    groups.forEach { group ->
                        FilterChip(
                            selected = selectedGroupId == group.id,
                            onClick = { selectedGroupId = group.id },
                            label = { Text(group.name) }
                        )
                    }
                }
            }

            SectionCard(title = stringResource(R.string.settings_section_appearance)) {
                Text(text = stringResource(R.string.add_account_theme_color_label), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .selectable(
                                    selected = isSelected,
                                    onClick = { selectedColor = color },
                                    role = Role.RadioButton
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.add_account_color_selected_content_description),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val bal = (calculatedBalance?.multiply(java.math.BigDecimal(100))?.toLong()) ?: 0L
                    val invested = (calculatedInvestedAmount?.multiply(java.math.BigDecimal(100))?.toLong())

                    val account = AccountRecord(
                        userId = userId,
                        name = name,
                        type = type,
                        initialBalance = bal,
                        currency = currency,
                        groupId = selectedGroupId,
                        icon = null,
                        color = selectedColor,
                        investedAmount = invested,
                        interestRate = interestRate.toDoubleOrNull(),
                        bankName = bankName.takeIf { it.isNotBlank() },
                        maturityDate = maturityDate,
                        personName = personName.takeIf { it.isNotBlank() },
                        dueDate = dueDate,
                        isReceivable = if (type == AccountType.RECEIVABLE || type == AccountType.LIABILITY) type == AccountType.RECEIVABLE else null
                    )
                    onSaveClick(account)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && balanceExpr.isNotBlank()
            ) {
                Text(stringResource(R.string.add_account_save_button))
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun AddAccountScreenPreview() {
    val mockGroups = listOf(
        AccountGroup(id = 1, userId = 1, name = "Banks"),
        AccountGroup(id = 2, userId = 1, name = "Wallets")
    )
    PersonalMoneyManagemntTheme {
        AddAccountScreen(
            groups = mockGroups,
            userId = 1,
            onSaveClick = {},
            onBackClick = {}
        )
    }
}
