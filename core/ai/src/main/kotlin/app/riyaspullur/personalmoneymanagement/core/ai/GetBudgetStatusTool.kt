package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.CalculateBudgetStatusUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetBudgetStatusTool @Inject constructor(
    private val calculateBudgetStatusUseCase: CalculateBudgetStatusUseCase
) : AiAssistantTool {
    override val name = "get_budget_status"
    override val description = "Get the current budget status, including remaining amount and daily allowance"
    override val parameters = mapOf("userId" to "Long")

    override suspend fun execute(arguments: Map<String, Any?>): AiToolResult {
        val userId = arguments["userId"] as? Long ?: return AiToolResult.Error("Missing userId")
        
        return try {
            val status = calculateBudgetStatusUseCase(userId).firstOrNull()
            if (status != null) {
                AiToolResult.Success(mapOf(
                    "limit" to status.budget.totalLimit.amount,
                    "spent" to status.spent.amount,
                    "remaining" to status.remaining.amount,
                    "daysRemaining" to status.daysRemaining,
                    "dailyAllowance" to status.dailyAllowance.amount,
                    "currency" to status.budget.totalLimit.currency.code
                ))
            } else {
                AiToolResult.Success("No active budget found")
            }
        } catch (e: Exception) {
            AiToolResult.Error(e.message ?: "Unknown error")
        }
    }
}
