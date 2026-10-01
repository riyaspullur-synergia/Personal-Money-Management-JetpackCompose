package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveUserByUsername() = runBlocking {
        dao.insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))

        val user = dao.getUserByUsername("riyas")

        assertEquals("Riyas", user?.displayName)
    }

    @Test
    fun getUserByUsernameReturnsNullWhenNotFound() = runBlocking {
        assertNull(dao.getUserByUsername("ghost"))
    }

    @Test
    fun getUserByIdStreamsTheUser() = runBlocking {
        val id = dao.insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))

        assertEquals("riyas", dao.getUserById(id).first()?.username)
    }

    @Test
    fun updateUserPersistsChanges() = runBlocking {
        val id = dao.insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
        val user = dao.getUserById(id).first()!!

        dao.updateUser(user.copy(displayName = "Riyas Pullur"))

        assertEquals("Riyas Pullur", dao.getUserById(id).first()?.displayName)
    }

    @Test
    fun getUserCountReflectsInsertedUsers() = runBlocking {
        assertEquals(0, dao.getUserCount())

        dao.insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
        dao.insertUser(UserEntity(username = "guest", passwordHash = "hash", displayName = "Guest"))

        assertEquals(2, dao.getUserCount())
    }

    @Test
    fun getAllUsersReturnsEveryUser() = runBlocking {
        dao.insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
        dao.insertUser(UserEntity(username = "guest", passwordHash = "hash", displayName = "Guest"))

        val users = dao.getAllUsers().first()

        assertEquals(setOf("riyas", "guest"), users.map { it.username }.toSet())
    }
}
