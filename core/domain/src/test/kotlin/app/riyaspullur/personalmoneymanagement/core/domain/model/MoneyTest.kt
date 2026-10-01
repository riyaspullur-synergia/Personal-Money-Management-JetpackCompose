package app.riyaspullur.personalmoneymanagement.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class MoneyTest {

    @Before
    fun setup() {
        // Set default locale to US for consistent test results
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `Money addition works correctly`() {
        val money1 = Money(100, Currency.AED)
        val money2 = Money(200, Currency.AED)
        val result = money1 + money2
        assertEquals(300L, result.amount)
        assertEquals(Currency.AED, result.currency)
    }

    @Test
    fun `Money subtraction works correctly`() {
        val money1 = Money(500, Currency.AED)
        val money2 = Money(200, Currency.AED)
        val result = money1 - money2
        assertEquals(300L, result.amount)
        assertEquals(Currency.AED, result.currency)
    }

    @Test
    fun `Money multiplication works correctly`() {
        val money = Money(100, Currency.AED)
        val result = money * 1.5
        assertEquals(150L, result.amount)
    }

    @Test
    fun `Money formatting works correctly`() {
        val money = Money(12345, Currency.AED)
        // en_US formatting for AED usually results in "AED 123.45" or similar
        // Note: some systems might use different spacing or currency symbols
        val formatted = money.format()
        assertTrue("Formatted value '$formatted' should contain 123.45", formatted.contains("123.45"))
        assertTrue("Formatted value '$formatted' should contain AED", formatted.contains("AED"))
        
        assertEquals("123.45", money.format(includeSymbol = false))
    }

    @Test
    fun `Money from major units works correctly`() {
        val money = Money.fromMajorUnits(123.45, Currency.AED)
        assertEquals(12345L, money.amount)
    }

    @Test
    fun `Money from string major units works correctly`() {
        val money = Money.fromMajorUnits("123.45", Currency.AED)
        assertEquals(12345L, money.amount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Adding different currencies throws exception`() {
        val aed = Money(100, Currency.AED)
        val usd = Money(100, Currency.USD)
        aed + usd
    }
}
