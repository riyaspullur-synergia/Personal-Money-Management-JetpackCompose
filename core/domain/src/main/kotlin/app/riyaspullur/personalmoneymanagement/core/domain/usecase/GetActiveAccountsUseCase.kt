package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(userId: Long): Flow<List<AccountRecord>> = 
        accountRepository.getActiveAccounts(userId)
}
