package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiToolResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAssistantToolDispatcherTest {

    private class FakeTool(override val name: String) : AiAssistantTool {
        override val description = "Fake tool"
        override val parameters = emptyMap<String, String>()
        override suspend fun execute(arguments: Map<String, Any?>): AiToolResult {
            return AiToolResult.Success("Executed $name")
        }
    }

    @Test
    fun `executeTool returns success when tool exists`() = runTest {
        val tool = FakeTool("test_tool")
        val dispatcher = AiAssistantToolDispatcher(setOf(tool))
        
        val result = dispatcher.executeTool("test_tool", emptyMap())
        
        assertTrue(result is AiToolResult.Success)
        assertEquals("Executed test_tool", (result as AiToolResult.Success).data)
    }

    @Test
    fun `executeTool returns error when tool does not exist`() = runTest {
        val dispatcher = AiAssistantToolDispatcher(emptySet())
        
        val result = dispatcher.executeTool("unknown", emptyMap())
        
        assertTrue(result is AiToolResult.Error)
        assertEquals("Tool not found: unknown", (result as AiToolResult.Error).message)
    }
}
