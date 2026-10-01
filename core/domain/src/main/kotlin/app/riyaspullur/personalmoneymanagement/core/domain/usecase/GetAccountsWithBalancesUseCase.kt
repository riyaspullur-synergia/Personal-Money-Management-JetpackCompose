package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsWithBalancesUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(userId: Long): Flow<List<Account>> = 
        accountRepository.getActiveAccountsWithBalances(userId)
}
