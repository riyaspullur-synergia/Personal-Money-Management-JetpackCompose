package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantClient
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantToolDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AiAssistantUseCase @Inject constructor(
    private val client: AiAssistantClient,
    private val toolDispatcher: AiAssistantToolDispatcher
) {
    fun getSystemInstruction(): String {
        val toolDefinitions = toolDispatcher.getToolDefinitions()
        return """
            You are a helpful financial assistant for a personal money management app.
            You are powered by Google Gemini.
            You MUST use tools to get accurate financial data. Never guess or invent numbers.
            To use a tool, output ONLY the tool call in this format: TOOL: tool_name(param1=value1)
            Available tools:
            $toolDefinitions
            
            When a tool returns data, explain it naturally and helpfully to the user.
        """.trimIndent()
    }

    suspend fun execute(userId: Long, userMessage: String): Flow<String> = flow {
        if (!client.isInitialized()) {
            emit("AI Assistant is not ready. Please set your Gemini API Key in the chat window.")
            return@flow
        }

        // First pass: Ask Gemini if it needs a tool
        val response = try {
            client.generateResponse(userMessage)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
        
        if (response.contains("TOOL:")) {
            val toolLine = response.lines().firstOrNull { it.contains("TOOL:") } ?: ""
            val toolCall = toolLine.substringAfter("TOOL:").trim()
            val toolName = toolCall.substringBefore("(").trim()
            
            // Basic argument parsing: search_transactions(query="value")
            val args = mutableMapOf<String, Any?>("userId" to userId)
            if (toolCall.contains("(") && toolCall.contains(")")) {
                val paramsString = toolCall.substringAfter("(").substringBeforeLast(")")
                paramsString.split(",").forEach { param ->
                    val parts = param.split("=")
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")
                        args[key] = value
                    }
                }
            }
            
            emit("Thinking (using $toolName)...")
            val toolResult = toolDispatcher.executeTool(toolName, args)
            
            val followUpPrompt = """
                The tool '$toolName' returned: $toolResult
                User original query: $userMessage
                Provide a complete and helpful answer based on this data.
            """.trimIndent()
            
            emitAll(client.generateResponseStream(followUpPrompt))
        } else {
            emit(response)
        }
    }
}
