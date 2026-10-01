package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountGroupDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountGroupEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountGroupRepositoryImplTest {

    private val dao: AccountGroupDao = mockk()
    private val repository = AccountGroupRepositoryImpl(dao)

    @Test
    fun `getActiveGroups delegates to dao`() = runTest {
        val groups = listOf(AccountGroupEntity(id = 1, userId = 1, name = "Banks"))
        every { dao.getActiveGroups(1L) } returns flowOf(groups)

        repository.getActiveGroups(1L).test {
            assertEquals(groups.map { it.toDomain() }, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertGroup delegates to dao and returns the new id`() = runTest {
        val group = AccountGroupEntity(userId = 1, name = "Banks")
        coEvery { dao.insertGroup(group) } returns 9L

        val id = repository.insertGroup(group.toDomain())

        assertEquals(9L, id)
        coVerify { dao.insertGroup(group) }
    }

    @Test
    fun `updateGroup delegates to dao`() = runTest {
        val group = AccountGroupEntity(id = 1, userId = 1, name = "Banks")
        coEvery { dao.updateGroup(group) } returns Unit

        repository.updateGroup(group.toDomain())

        coVerify { dao.updateGroup(group) }
    }

    @Test
    fun `deleteGroup delegates to dao`() = runTest {
        val group = AccountGroupEntity(id = 1, userId = 1, name = "Banks")
        coEvery { dao.deleteGroup(group) } returns Unit

        repository.deleteGroup(group.toDomain())

        coVerify { dao.deleteGroup(group) }
    }
}
