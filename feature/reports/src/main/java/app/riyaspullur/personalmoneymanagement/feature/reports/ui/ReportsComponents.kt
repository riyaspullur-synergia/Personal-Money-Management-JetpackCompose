package app.riyaspullur.personalmoneymanagement.feature.reports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategoryReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.components.BarChart
import app.riyaspullur.personalmoneymanagement.core.ui.components.BarChartData
import app.riyaspullur.personalmoneymanagement.core.ui.components.HorizontalBarChart
import app.riyaspullur.personalmoneymanagement.core.ui.components.HorizontalBarData
import app.riyaspullur.personalmoneymanagement.core.ui.components.MoneyText
import app.riyaspullur.personalmoneymanagement.core.ui.components.PieChart
import app.riyaspullur.personalmoneymanagement.core.ui.components.PieChartData
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel
import app.riyaspullur.personalmoneymanagement.ui.theme.finance

@Composable
fun SummarySection(report: MonthlyReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.common_income), style = MaterialTheme.typography.bodyMedium)
                MoneyText(money = report.totalIncome, color = MaterialTheme.finance.income)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.reports_expenses_label), style = MaterialTheme.typography.bodyMedium)
                MoneyText(money = report.totalExpense, color = MaterialTheme.finance.expense)
            }
            if (report.totalDeposit.amount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(R.string.common_deposit), style = MaterialTheme.typography.bodyMedium)
                    MoneyText(money = report.totalDeposit, color = MaterialTheme.finance.savingsGoal)
                }
            }
            if (report.totalInvestment.amount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(R.string.common_investment), style = MaterialTheme.typography.bodyMedium)
                    MoneyText(money = report.totalInvestment, color = MaterialTheme.finance.chartPalette[5])
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.reports_net_balance_label), style = MaterialTheme.typography.titleMedium)
                MoneyText(money = report.netBalance, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun DailyTrendSection(report: MonthlyReport) {
    val dayFormat = java.text.SimpleDateFormat("d", LocalLocale.current.platformLocale)
    val incomeColor = MaterialTheme.finance.income
    val expenseColor = MaterialTheme.finance.expense
    
    val combinedData = mutableListOf<BarChartData>()
    
    // Process last 10 days of activity
    val allDates = (report.dailyExpenses.map { it.date } + report.dailyIncomes.map { it.date }).distinct().sorted().takeLast(10)
    
    allDates.forEach { date ->
        val income = report.dailyIncomes.find { it.date == date }?.amount?.toBigDecimal()?.toFloat() ?: 0f
        val expense = report.dailyExpenses.find { it.date == date }?.amount?.toBigDecimal()?.toFloat() ?: 0f
        val label = dayFormat.format(java.util.Date(date))
        
        if (income > 0) {
            combinedData.add(BarChartData(value = income, color = incomeColor, label = label))
        }
        if (expense > 0) {
            combinedData.add(BarChartData(value = expense, color = expenseColor, label = label))
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.reports_daily_trend_title),
                    style = MaterialTheme.typography.titleSmall
                )

                // Mini Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(stringResource(R.string.common_income), incomeColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendItem(stringResource(R.string.common_expense), expenseColor)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (combinedData.isNotEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    BarChart(data = combinedData)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.reports_no_activity_label), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PieChartSection(report: MonthlyReport) {
    val chartColors = MaterialTheme.finance.chartPalette

    val chartData = report.categoryBreakdown.mapIndexed { index, it ->
        PieChartData(
            value = it.amount.toBigDecimal().toFloat(),
            color = chartColors[index % chartColors.size]
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.reports_spending_breakdown_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center) {
                PieChart(
                    data = chartData,
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 50f
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.common_total), style = MaterialTheme.typography.labelSmall)
                    MoneyText(
                        money = report.totalExpense,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.finance.expense
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                report.categoryBreakdown.take(6).forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            color = chartColors[index % chartColors.size],
                            shape = CircleShape
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.category?.displayLabel() ?: stringResource(R.string.reports_uncategorized_label),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBarChartSection(report: MonthlyReport) {
    val chartColors = MaterialTheme.finance.chartPalette
    val uncategorizedLabel = stringResource(R.string.reports_uncategorized_label)
    val chartData = report.categoryBreakdown.take(5).mapIndexed { index, it ->
        HorizontalBarData(
            label = it.category?.displayLabel() ?: uncategorizedLabel,
            value = it.amount.toBigDecimal().toFloat(),
            money = it.amount,
            color = chartColors[index % chartColors.size]
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.reports_expense_breakdown_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            HorizontalBarChart(data = chartData)
        }
    }
}

@Composable
fun CategoryBreakdownItem(item: CategoryReport, index: Int) {
    val chartColors = MaterialTheme.finance.chartPalette
    val categoryColor = chartColors[index % chartColors.size]
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = categoryColor,
            shape = MaterialTheme.shapes.extraSmall
        ) {}
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.category?.displayLabel() ?: stringResource(R.string.reports_uncategorized_label),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${(item.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        MoneyText(money = item.amount, style = MaterialTheme.typography.bodyMedium)
    }
}
