package app.riyaspullur.personalmoneymanagement.core.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceValidatorTest {

    @Test
    fun `isValidAmount is true for positive amounts`() {
        assertTrue(FinanceValidator.isValidAmount(1L))
    }

    @Test
    fun `isValidAmount is false for zero`() {
        assertFalse(FinanceValidator.isValidAmount(0L))
    }

    @Test
    fun `isValidAmount is false for negative amounts`() {
        assertFalse(FinanceValidator.isValidAmount(-1L))
    }

    @Test
    fun `isValidBudgetAmount allows zero`() {
        assertTrue(FinanceValidator.isValidBudgetAmount(0L))
    }

    @Test
    fun `isValidBudgetAmount allows positive amounts`() {
        assertTrue(FinanceValidator.isValidBudgetAmount(1000L))
    }

    @Test
    fun `isValidBudgetAmount rejects negative amounts`() {
        assertFalse(FinanceValidator.isValidBudgetAmount(-1L))
    }

    @Test
    fun `isValidDateRange is true when end is after start`() {
        assertTrue(FinanceValidator.isValidDateRange(100L, 200L))
    }

    @Test
    fun `isValidDateRange is true when start equals end`() {
        assertTrue(FinanceValidator.isValidDateRange(100L, 100L))
    }

    @Test
    fun `isValidDateRange is false when end is before start`() {
        assertFalse(FinanceValidator.isValidDateRange(200L, 100L))
    }

    @Test
    fun `validateTransaction fails for zero amount`() {
        val result = FinanceValidator.validateTransaction(0L, "Coffee")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Amount must be greater than zero", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateTransaction fails for negative amount`() {
        val result = FinanceValidator.validateTransaction(-500L, "Coffee")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `validateTransaction fails for description longer than 200 characters`() {
        val longDescription = "a".repeat(201)
        val result = FinanceValidator.validateTransaction(1000L, longDescription)
        assertTrue(result is ValidationResult.Error)
        assertEquals("Description too long", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateTransaction succeeds for description exactly 200 characters`() {
        val description = "a".repeat(200)
        val result = FinanceValidator.validateTransaction(1000L, description)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateTransaction succeeds with null description`() {
        val result = FinanceValidator.validateTransaction(1000L, null)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateTransaction checks amount before description length`() {
        val longDescription = "a".repeat(201)
        val result = FinanceValidator.validateTransaction(0L, longDescription)
        assertEquals("Amount must be greater than zero", (result as ValidationResult.Error).message)
    }
}
