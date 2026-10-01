package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class GetMonthlyExpenseTool @Inject constructor(
    private val transactionRepository: TransactionRepository
) : AiAssistantTool {
    override val name = "get_monthly_expense"
    override val description = "Get the total expense for the current month"
    override val parameters = mapOf("userId" to "Long")

    override suspend fun execute(arguments: Map<String, Any?>): AiToolResult {
        val userId = arguments["userId"] as? Long ?: return AiToolResult.Error("Missing userId")
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis
        val endOfMonth = System.currentTimeMillis()

        return try {
            val total = transactionRepository.getTotalAmountByType(
                userId = userId,
                type = TransactionType.EXPENSE,
                startDate = startOfMonth,
                endDate = endOfMonth
            ).first() ?: 0L
            
            AiToolResult.Success(mapOf("total" to total, "period" to "current month"))
        } catch (e: Exception) {
            AiToolResult.Error(e.message ?: "Unknown error")
        }
    }
}
