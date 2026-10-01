package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountGroupsUseCase @Inject constructor(
    private val accountGroupRepository: AccountGroupRepository
) {
    operator fun invoke(userId: Long): Flow<List<AccountGroup>> = 
        accountGroupRepository.getActiveGroups(userId)
}
