package app.riyaspullur.personalmoneymanagement.core.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class CsvImporterTest {

    private val context: Context = mockk()
    private val contentResolver: ContentResolver = mockk()
    private val importer = CsvImporter(context)

    @Test
    fun `parseCsv correctly parses a valid CSV string`() = runTest {
        val csvData = """
            Date,Type,Amount,Currency,Account Name,Category,Merchant,Description,Status
            2026-08-20 10:00,EXPENSE,100.0,AED,Cash,Food,Supermarket,Weekly food,CLEARED
            2026-08-21 12:00,INCOME,5000.0,AED,Bank,Salary,Employer,Monthly salary,CLEARED
        """.trimIndent()

        val uri: Uri = mockk()
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(csvData.toByteArray())

        val result = importer.parseTransactions(uri)

        assertEquals(2, result.size)
        assertEquals("2026-08-20 10:00", result[0].date)
        assertEquals("EXPENSE", result[0].type)
        assertEquals("100.0", result[0].amount)
        assertEquals("AED", result[0].currency)
        assertEquals("Cash", result[0].accountName)
        assertEquals("Food", result[0].categoryName)
        assertEquals("Supermarket", result[0].merchant)
        assertEquals("Weekly food", result[0].description)
        assertEquals("CLEARED", result[0].status)

        assertEquals("2026-08-21 12:00", result[1].date)
        assertEquals("INCOME", result[1].type)
    }

    @Test
    fun `parseCsv returns empty list for empty stream`() = runTest {
        val uri: Uri = mockk()
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(uri) } returns null

        val result = importer.parseTransactions(uri)

        assertEquals(0, result.size)
    }
}
