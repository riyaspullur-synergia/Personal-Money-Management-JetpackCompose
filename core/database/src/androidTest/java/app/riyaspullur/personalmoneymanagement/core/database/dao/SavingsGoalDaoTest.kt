package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.SavingsGoalEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavingsGoalDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SavingsGoalDao
    private var userId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.savingsGoalDao()
        userId = db.userDao().insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun goal(name: String, completed: Boolean = false) = SavingsGoalEntity(
        userId = userId, name = name, targetAmount = 100000L, currentAmount = 0L,
        currency = Currency.AED, targetDate = null, icon = null, color = 0, isCompleted = completed
    )

    @Test
    fun getActiveGoalsExcludesCompletedGoals() = runBlocking {
        dao.insertGoal(goal("Car"))
        dao.insertGoal(goal("Done", completed = true))

        val goals = dao.getActiveGoals(userId).first()

        assertEquals(listOf("Car"), goals.map { it.name })
    }

    @Test
    fun updateGoalPersistsChanges() = runBlocking {
        val id = dao.insertGoal(goal("Car"))
        val stored = dao.getActiveGoals(userId).first().single()

        dao.updateGoal(stored.copy(name = "New Car"))

        assertEquals("New Car", dao.getActiveGoals(userId).first().single { it.id == id }.name)
    }

    @Test
    fun contributeToGoalIncrementsCurrentAmount() = runBlocking {
        val id = dao.insertGoal(goal("Car"))

        dao.contributeToGoal(id, 5000L)
        dao.contributeToGoal(id, 2500L)

        assertEquals(7500L, dao.getActiveGoals(userId).first().single().currentAmount)
    }
}
