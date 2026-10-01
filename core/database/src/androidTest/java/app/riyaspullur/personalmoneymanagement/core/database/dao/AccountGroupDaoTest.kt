package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountGroupEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountGroupDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AccountGroupDao
    private var userId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
            .build()
        dao = db.accountGroupDao()
        userId = db.userDao().insertUser(
            UserEntity(
                username = "riyas",
                passwordHash = "hash",
                displayName = "Riyas"
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getActiveGroupsExcludesArchivedAndOrdersBySortOrder() = runBlocking {
        dao.insertGroup(AccountGroupEntity(userId = userId, name = "Second", sortOrder = 1))
        dao.insertGroup(AccountGroupEntity(userId = userId, name = "First", sortOrder = 0))
        dao.insertGroup(
            AccountGroupEntity(
                userId = userId,
                name = "Archived",
                sortOrder = 2,
                isArchived = true
            )
        )

        val groups = dao.getActiveGroups(userId).first()

        assertEquals(listOf("First", "Second"), groups.map { it.name })
    }

    @Test
    fun updateGroupPersistsChanges() = runBlocking {
        val id = dao.insertGroup(AccountGroupEntity(userId = userId, name = "Banks"))
        val group = dao.getActiveGroups(userId).first().single()

        dao.updateGroup(group.copy(name = "Primary Banks"))

        assertEquals(
            "Primary Banks",
            dao.getActiveGroups(userId).first().single { it.id == id }.name
        )
    }

    @Test
    fun deleteGroupRemovesIt() = runBlocking {
        dao.insertGroup(AccountGroupEntity(userId = userId, name = "Banks"))
        val group = dao.getActiveGroups(userId).first().single()

        dao.deleteGroup(group)

        assertEquals(0, dao.getActiveGroups(userId).first().size)
    }
}
