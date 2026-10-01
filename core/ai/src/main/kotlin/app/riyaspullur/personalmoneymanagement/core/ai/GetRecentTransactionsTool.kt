package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetRecentTransactionsTool @Inject constructor(
    private val transactionRepository: TransactionRepository
) : AiAssistantTool {
    override val name = "get_recent_transactions"
    override val description = "Get the 10 most recent transactions for the user"
    override val parameters = mapOf("userId" to "Long")

    override suspend fun execute(arguments: Map<String, Any?>): AiToolResult {
        val userId = arguments["userId"] as? Long ?: return AiToolResult.Error("Missing userId")
        
        return try {
            val transactions = transactionRepository.getAllTransactions(userId).first()
                .filter { !it.isDeleted }
                .sortedByDescending { it.transactionDate }
                .take(10)
            
            val formattedTransactions = transactions.map {
                mapOf(
                    "amount" to it.amount,
                    "type" to it.type.name,
                    "description" to (it.description ?: it.merchant ?: "No description"),
                    "date" to it.transactionDate,
                    "status" to it.paymentStatus
                )
            }
            
            AiToolResult.Success(mapOf("transactions" to formattedTransactions))
        } catch (e: Exception) {
            AiToolResult.Error(e.message ?: "Unknown error")
        }
    }
}
