package app.riyaspullur.personalmoneymanagement.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic finance colors, deliberately separate from the M3 [androidx.compose.material3.ColorScheme]
 * roles so "this is income/expense" never gets visually confused with "this is a
 * destructive/error action" (e.g. deleting a transaction).
 */
data class FinanceColors(
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val savingsGoal: Color,
    val chartPalette: List<Color>
)

val LightFinanceColors = FinanceColors(
    income = IncomeLight,
    expense = ExpenseLight,
    transfer = TransferLight,
    savingsGoal = SavingsGoalLight,
    chartPalette = ChartPaletteLight
)

val DarkFinanceColors = FinanceColors(
    income = IncomeDark,
    expense = ExpenseDark,
    transfer = TransferDark,
    savingsGoal = SavingsGoalDark,
    chartPalette = ChartPaletteDark
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

val MaterialTheme.finance: FinanceColors
    @Composable
    get() = LocalFinanceColors.current
