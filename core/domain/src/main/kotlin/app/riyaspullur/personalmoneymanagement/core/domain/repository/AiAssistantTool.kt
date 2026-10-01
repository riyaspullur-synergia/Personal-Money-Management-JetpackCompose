package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult

interface AiAssistantTool {
    val name: String
    val description: String
    val parameters: Map<String, String>

    suspend fun execute(arguments: Map<String, Any?>): AiToolResult
}
