package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantToolDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAssistantToolDispatcher @Inject constructor(
    private val tools: Set<@JvmSuppressWildcards AiAssistantTool>
) : AiAssistantToolDispatcher {
    override suspend fun executeTool(name: String, arguments: Map<String, Any?>): AiToolResult {
        val tool = tools.find { it.name == name }
            ?: return AiToolResult.Error("Tool not found: $name")
        
        return try {
            tool.execute(arguments)
        } catch (e: Exception) {
            AiToolResult.Error("Tool execution failed: ${e.message}")
        }
    }

    override fun getToolDefinitions(): String {
        return tools.joinToString("\n") { tool ->
            "- ${tool.name}: ${tool.description}. Parameters: ${tool.parameters}"
        }
    }
}
