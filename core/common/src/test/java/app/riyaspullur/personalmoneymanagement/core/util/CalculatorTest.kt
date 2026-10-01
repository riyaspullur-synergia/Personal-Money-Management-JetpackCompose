package app.riyaspullur.personalmoneymanagement.core.util

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CalculatorTest {

    private fun assertNumericallyEquals(expected: String, actual: BigDecimal?) {
        assertTrue("Expected $expected but was $actual", actual != null && actual.compareTo(BigDecimal(expected)) == 0)
    }

    @Test
    fun `addition returns sum`() {
        assertNumericallyEquals("5", Calculator.evaluate("2+3"))
    }

    @Test
    fun `subtraction returns difference`() {
        assertNumericallyEquals("6", Calculator.evaluate("10-4"))
    }

    @Test
    fun `multiplication returns product`() {
        assertNumericallyEquals("12", Calculator.evaluate("3*4"))
    }

    @Test
    fun `division returns quotient`() {
        assertNumericallyEquals("2.5", Calculator.evaluate("10/4"))
    }

    @Test
    fun `division result is rounded to 2 decimal places`() {
        assertNumericallyEquals("3.33", Calculator.evaluate("10/3"))
    }

    @Test
    fun `operators are evaluated strictly left to right without precedence`() {
        // (2+3)*4 = 20, NOT 2+(3*4) = 14
        assertNumericallyEquals("20", Calculator.evaluate("2+3*4"))
    }

    @Test
    fun `dividing by zero leaves the running result unchanged`() {
        assertNumericallyEquals("5", Calculator.evaluate("5/0"))
    }

    @Test
    fun `chained operations apply sequentially`() {
        assertNumericallyEquals("5", Calculator.evaluate("10-2-3"))
    }

    @Test
    fun `decimal operands are supported`() {
        assertNumericallyEquals("3.75", Calculator.evaluate("1.5+2.25"))
    }

    @Test
    fun `spaces in the expression are ignored`() {
        assertNumericallyEquals("5", Calculator.evaluate("2 + 3"))
    }

    @Test
    fun `single number with no operator returns that number`() {
        assertNumericallyEquals("42", Calculator.evaluate("42"))
    }

    @Test
    fun `empty expression returns null`() {
        assertNull(Calculator.evaluate(""))
    }

    @Test
    fun `whitespace only expression returns null`() {
        assertNull(Calculator.evaluate("   "))
    }

    @Test
    fun `non numeric expression returns null`() {
        assertNull(Calculator.evaluate("abc"))
    }

    @Test
    fun `trailing operator returns null`() {
        assertNull(Calculator.evaluate("5+"))
    }

    @Test
    fun `leading operator returns null`() {
        assertNull(Calculator.evaluate("+5"))
    }

    @Test
    fun `result is stripped of trailing zeros`() {
        val result = Calculator.evaluate("2+2")
        assertNumericallyEquals("4", result)
    }
}
