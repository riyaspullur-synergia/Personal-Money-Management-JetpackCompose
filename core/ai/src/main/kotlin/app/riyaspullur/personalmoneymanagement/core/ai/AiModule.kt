package app.riyaspullur.personalmoneymanagement.core.ai

import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantTool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    abstract fun bindClient(impl: AiAssistantEngine): app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantClient

    @Binds
    abstract fun bindDispatcher(impl: AiAssistantToolDispatcher): app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantToolDispatcher

    @Binds
    @IntoSet
    abstract fun bindGetMonthlyExpenseTool(tool: GetMonthlyExpenseTool): AiAssistantTool

    @Binds
    @IntoSet
    abstract fun bindGetBudgetStatusTool(tool: GetBudgetStatusTool): AiAssistantTool

    @Binds
    @IntoSet
    abstract fun bindGetRecentTransactionsTool(tool: GetRecentTransactionsTool): AiAssistantTool

    @Binds
    @IntoSet
    abstract fun bindSearchTransactionsTool(tool: SearchTransactionsTool): AiAssistantTool
}
