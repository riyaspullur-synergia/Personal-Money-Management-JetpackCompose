package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountGroupRepository
import javax.inject.Inject

class AddAccountGroupUseCase @Inject constructor(
    private val accountGroupRepository: AccountGroupRepository
) {
    suspend operator fun invoke(group: AccountGroup) {
        accountGroupRepository.insertGroup(group)
    }
}
