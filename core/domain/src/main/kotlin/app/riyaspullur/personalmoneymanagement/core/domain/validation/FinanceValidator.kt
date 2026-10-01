package app.riyaspullur.personalmoneymanagement.core.domain.validation

object FinanceValidator {
    fun isValidAmount(amount: Long): Boolean = amount > 0
    
    fun isValidBudgetAmount(amount: Long): Boolean = amount >= 0
    
    fun isValidDateRange(startDate: Long, endDate: Long): Boolean = endDate >= startDate
    
    fun validateTransaction(amount: Long, description: String?): ValidationResult {
        if (amount <= 0) return ValidationResult.Error("Amount must be greater than zero")
        if (description != null && description.length > 200) return ValidationResult.Error("Description too long")
        return ValidationResult.Success
    }
}

sealed interface ValidationResult {
    data object Success : ValidationResult
    data class Error(val message: String) : ValidationResult
}
