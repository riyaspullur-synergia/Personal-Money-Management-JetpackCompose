package app.riyaspullur.personalmoneymanagement.core.export

import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupArchive
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupData
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetCategoryLimit
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.model.validate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class BackupCodecTest {
    private val codec = BackupCodec()
    private val archive = BackupArchive(
        createdAt = 123456789,
        displayName = "Riya, \"عربي\"\nമലയാളം 😀",
        themeMode = "DARK", language = "ml",
        data = BackupData(
            accountGroups = listOf(AccountGroup(8, 1, "Group", 7, true)),
            accounts = listOf(AccountRecord(
                id = 3, userId = 1, groupId = 8, name = "Savings", type = AccountType.entries.first(),
                initialBalance = 9_007_199_254_740_993, currency = Currency.AED, icon = null, color = -123,
                isArchived = true, createdAt = 11, investedAmount = 23, lastValuationDate = 24,
                interestRate = 3.25, maturityDate = 25, bankName = "Bank", personName = "Person",
                dueDate = 26, isReceivable = false
            )),
            categories = listOf(Category(4, 1, "Category", "icon", 123, TransactionType.EXPENSE, isArchived = true)),
            transactions = listOf(Transaction(
                id = 5, userId = 1, accountId = 3, toAccountId = 3, amount = Long.MAX_VALUE,
                currency = Currency.INR, categoryId = 4, type = TransactionType.TRANSFER,
                merchant = "=SUM(A1:A2)", description = "a,b\r\nc\"d", notes = "😀 العربية മലയാളം\n".repeat(4000),
                transactionDate = 123, createdAt = 124, updatedAt = 125, paymentStatus = "PENDING",
                receiptPath = "/old/phone/receipt", isRecurring = true, isDeleted = true
            )),
            budgets = listOf(BudgetRecord(6, 1, "Budget", 555, Currency.USD, 12, 34, 0.75f, true, 56)),
            budgetCategoryLimits = listOf(BudgetCategoryLimit(7, 6, 4, 444)),
            savingsGoals = listOf(SavingsGoal(9, 1, "Goal", 999, 777, Currency.EUR, 123, null, 11, true, 456))
        ),
        receipts = mapOf("5" to "YWJj".repeat(10000))
    )

    @Test fun `CSV preserves every field including large integers unicode and multiline text`() {
        roundTrip(BackupFormat.CSV)
    }

    @Test fun `Excel preserves every field and chunked attachments`() {
        roundTrip(BackupFormat.XLSX)
    }

    @Test fun `empty backup round trips in both formats`() {
        BackupFormat.entries.forEach { format ->
            val empty = archive.copy(data = BackupData(), receipts = emptyMap())
            val output = ByteArrayOutputStream()
            codec.write(empty, format, output)
            assertEquals(empty, codec.read(output.toByteArray().inputStream()))
        }
    }

    @Test fun `edited truncated and report CSV files are rejected`() {
        val output = ByteArrayOutputStream()
        codec.write(archive, BackupFormat.CSV, output)
        val text = output.toString("UTF-8")
        listOf(text.replace("Savings", "Edited"), text.substringBeforeLast("\"'END\""), "Date,Amount\n2026-01-01,10").forEach {
            assertThrows(IllegalArgumentException::class.java) { codec.read(it.byteInputStream()) }
        }
    }

    @Test fun `CSV cells are escaped as text to avoid spreadsheet formulas`() {
        val output = ByteArrayOutputStream()
        codec.write(archive, BackupFormat.CSV, output)
        assertTrue(output.toString("UTF-8").lineSequence().filter { it.isNotEmpty() }.all { it.startsWith("\"'") })
    }

    @Test fun `invalid relationships duplicate identifiers and missing receipts are rejected`() {
        archive.validate()
        val invalid = listOf(
            archive.copy(data = archive.data.copy(accounts = emptyList())),
            archive.copy(data = archive.data.copy(categories = archive.data.categories + archive.data.categories)),
            archive.copy(data = archive.data.copy(transactions = archive.data.transactions.map { it.copy(toAccountId = 999) })),
            archive.copy(data = archive.data.copy(categories = archive.data.categories.map { it.copy(parentCategoryId = it.id) })),
            archive.copy(receipts = emptyMap()), archive.copy(version = 99)
        )
        invalid.forEach { assertThrows(IllegalArgumentException::class.java) { it.validate() } }
    }

    @Test fun `Excel external entities are rejected`() {
        val output = ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(output).use {
            it.putNextEntry(java.util.zip.ZipEntry("xl/worksheets/sheet1.xml"))
            it.write("<!DOCTYPE foo [<!ENTITY x SYSTEM 'file:///secret'>]><foo>&x;</foo>".toByteArray())
            it.closeEntry()
        }
        assertThrows(IllegalArgumentException::class.java) { codec.read(output.toByteArray().inputStream()) }
    }

    private fun roundTrip(format: BackupFormat) {
        val output = ByteArrayOutputStream()
        codec.write(archive, format, output)
        val restored = codec.read(output.toByteArray().inputStream())
        restored.validate()
        assertEquals(archive, restored)
    }
}
