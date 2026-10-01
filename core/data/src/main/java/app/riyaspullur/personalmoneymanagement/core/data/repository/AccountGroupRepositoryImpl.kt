package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountGroupDao
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountGroupRepositoryImpl @Inject constructor(
    private val accountGroupDao: AccountGroupDao
) : AccountGroupRepository {
    override fun getActiveGroups(userId: Long): Flow<List<AccountGroup>> =
        accountGroupDao.getActiveGroups(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAllGroupsForBackup(userId: Long): List<AccountGroup> =
        accountGroupDao.getAllGroups(userId).map { it.toDomain() }

    override suspend fun insertGroup(group: AccountGroup): Long =
        accountGroupDao.insertGroup(group.toEntity())

    override suspend fun updateGroup(group: AccountGroup) =
        accountGroupDao.updateGroup(group.toEntity())

    override suspend fun deleteGroup(group: AccountGroup) =
        accountGroupDao.deleteGroup(group.toEntity())
}
