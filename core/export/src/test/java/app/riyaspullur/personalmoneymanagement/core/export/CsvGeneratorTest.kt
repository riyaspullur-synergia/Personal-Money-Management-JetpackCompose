package app.riyaspullur.personalmoneymanagement.core.export

import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategoryReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CsvGeneratorTest {

    private val csvGenerator = CsvGenerator()

    private fun tempFile(name: String): File =
        File.createTempFile(name, ".csv").apply { deleteOnExit() }

    @Test
    fun `full report csv contains summary totals`() {
        val file = tempFile("summary")
        val report = MonthlyReport(
            totalIncome = Money(500000L, Currency.AED),
            totalExpense = Money(250000L, Currency.AED),
            netBalance = Money(250000L, Currency.AED),
            categoryBreakdown = emptyList(),
            dailyExpenses = emptyList(),
            dailyIncomes = emptyList()
        )

        csvGenerator.generateFullReportCsv(file, report, "August 2026")

        val content = file.readText()
        assertTrue(content.contains("Summary: August 2026"))
        assertTrue(content.contains("Total Income,5000.00"))
        assertTrue(content.contains("Total Expense,2500.00"))
        assertTrue(content.contains("Net Balance,2500.00"))
    }

    @Test
    fun `full report csv lists each category with amount and percentage`() {
        val file = tempFile("categories")
        val report = MonthlyReport(
            totalIncome = Money(0L, Currency.AED),
            totalExpense = Money(150000L, Currency.AED),
            netBalance = Money(-150000L, Currency.AED),
            categoryBreakdown = listOf(
                CategoryReport(
                    category = Category(id = 1, userId = 1, name = "Food", icon = null, color = 0xFFF44336.toInt(), type = TransactionType.EXPENSE),
                    amount = Money(100000L, Currency.AED),
                    percentage = 0.6667f
                ),
                CategoryReport(
                    category = null,
                    amount = Money(50000L, Currency.AED),
                    percentage = 0.3333f
                )
            ),
            dailyExpenses = emptyList(),
            dailyIncomes = emptyList()
        )

        csvGenerator.generateFullReportCsv(file, report, "August 2026")

        val content = file.readText()
        assertTrue(content.contains("Category,Amount,Percentage"))
        assertTrue(content.contains("Food,1000.00,66%"))
        assertTrue(content.contains("Other,500.00,33%"))
    }

    @Test
    fun `transactions csv contains a header row`() {
        val file = tempFile("transactions")

        csvGenerator.generateTransactionsCsv(file, emptyList())

        val content = file.readText()
        assertEquals("Date,Type,Amount,Currency,Description,Merchant,Status\n", content)
    }

    @Test
    fun `transactions csv formats each transaction`() {
        val file = tempFile("transactions")
        val tx = Transaction(
            id = 1,
            userId = 1,
            accountId = 1,
            amount = 15000L,
            currency = Currency.AED,
            categoryId = 1,
            type = TransactionType.EXPENSE,
            merchant = "Starbucks",
            description = "Coffee",
            notes = null,
            transactionDate = 0L,
            paymentStatus = "CLEARED"
        )

        csvGenerator.generateTransactionsCsv(file, listOf(tx))

        val content = file.readText()
        val dataLine = content.lines()[1]
        assertTrue(dataLine.contains("EXPENSE"))
        assertTrue(dataLine.contains("150.00"))
        assertTrue(dataLine.contains("AED"))
        assertTrue(dataLine.contains("\"Coffee\""))
        assertTrue(dataLine.contains("\"Starbucks\""))
        assertTrue(dataLine.endsWith("CLEARED"))
    }

    @Test
    fun `transactions csv handles null description and merchant as empty quoted strings`() {
        val file = tempFile("transactions")
        val tx = Transaction(
            id = 1,
            userId = 1,
            accountId = 1,
            amount = 1000L,
            currency = Currency.AED,
            categoryId = null,
            type = TransactionType.INCOME,
            merchant = null,
            description = null,
            notes = null,
            transactionDate = 0L
        )

        csvGenerator.generateTransactionsCsv(file, listOf(tx))

        val dataLine = file.readText().lines()[1]
        assertTrue(dataLine.contains(",\"\",\"\","))
    }

    @Test
    fun `transactions csv preserves order across multiple rows`() {
        val file = tempFile("transactions")
        val tx1 = Transaction(id = 1, userId = 1, accountId = 1, amount = 100L, currency = Currency.AED, categoryId = null, type = TransactionType.EXPENSE, merchant = null, description = "First", notes = null, transactionDate = 0L)
        val tx2 = Transaction(id = 2, userId = 1, accountId = 1, amount = 200L, currency = Currency.AED, categoryId = null, type = TransactionType.INCOME, merchant = null, description = "Second", notes = null, transactionDate = 0L)

        csvGenerator.generateTransactionsCsv(file, listOf(tx1, tx2))

        val lines = file.readText().lines()
        assertTrue(lines[1].contains("First"))
        assertTrue(lines[2].contains("Second"))
    }
}
