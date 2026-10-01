package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SearchTransactionsTool @Inject constructor(
    private val transactionRepository: TransactionRepository
) : AiAssistantTool {
    override val name = "search_transactions"
    override val description = "Search transactions by merchant name or description"
    override val parameters = mapOf("userId" to "Long", "query" to "String")

    override suspend fun execute(arguments: Map<String, Any?>): AiToolResult {
        val userId = arguments["userId"] as? Long ?: return AiToolResult.Error("Missing userId")
        val query = arguments["query"] as? String ?: return AiToolResult.Error("Missing query")
        
        return try {
            val transactions = transactionRepository.getAllTransactions(userId).first()
                .filter { !it.isDeleted && 
                    (it.merchant?.contains(query, ignoreCase = true) == true || 
                     it.description?.contains(query, ignoreCase = true) == true)
                }
                .sortedByDescending { it.transactionDate }
                .take(20)
            
            val formattedTransactions = transactions.map {
                mapOf(
                    "amount" to it.amount,
                    "type" to it.type.name,
                    "merchant" to (it.merchant ?: ""),
                    "description" to (it.description ?: ""),
                    "date" to it.transactionDate
                )
            }
            
            AiToolResult.Success(mapOf("results" to formattedTransactions, "query" to query))
        } catch (e: Exception) {
            AiToolResult.Error(e.message ?: "Unknown error")
        }
    }
}
