package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import kotlinx.coroutines.flow.Flow

interface AccountGroupRepository {
    fun getActiveGroups(userId: Long): Flow<List<AccountGroup>>
    suspend fun getAllGroupsForBackup(userId: Long): List<AccountGroup>
    suspend fun insertGroup(group: AccountGroup): Long
    suspend fun updateGroup(group: AccountGroup)
    suspend fun deleteGroup(group: AccountGroup)
}
