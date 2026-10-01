package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult

interface AiAssistantToolDispatcher {
    suspend fun executeTool(name: String, arguments: Map<String, Any?>): AiToolResult
    fun getToolDefinitions(): String
}
