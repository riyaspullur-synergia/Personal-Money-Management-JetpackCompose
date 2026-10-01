package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import javax.inject.Inject

class SeedDefaultDataUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: Long) {
        val defaultCategories = listOf(
            Category(userId = userId, name = "Food", type = TransactionType.EXPENSE, color = 0xFFF44336.toInt(), icon = "🍕"),
            Category(userId = userId, name = "Transport", type = TransactionType.EXPENSE, color = 0xFF2196F3.toInt(), icon = "🚗"),
            Category(userId = userId, name = "Salary", type = TransactionType.INCOME, color = 0xFF4CAF50.toInt(), icon = "💰"),
            Category(userId = userId, name = "Shopping", type = TransactionType.EXPENSE, color = 0xFFFFEB3B.toInt(), icon = "🛍️"),
            Category(userId = userId, name = "Bills", type = TransactionType.EXPENSE, color = 0xFF9C27B0.toInt(), icon = "📄")
        )

        defaultCategories.forEach {
            categoryRepository.insertCategory(it)
        }
    }
}
