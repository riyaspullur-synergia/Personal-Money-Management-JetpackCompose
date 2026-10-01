package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetCategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BudgetDao
    private var userId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.budgetDao()
        userId = db.userDao().insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun budget(name: String, start: Long, end: Long) = BudgetEntity(
        userId = userId, name = name, totalLimit = 500000L, currency = Currency.AED,
        startDate = start, endDate = end
    )

    @Test
    fun getAllBudgetsOrdersByStartDateDescending() = runBlocking {
        dao.insertBudget(budget("January", 100L, 200L))
        dao.insertBudget(budget("February", 300L, 400L))

        val budgets = dao.getAllBudgets(userId).first()

        assertEquals(listOf("February", "January"), budgets.map { it.name })
    }

    @Test
    fun getActiveBudgetReturnsTheBudgetCoveringTheGivenDate() = runBlocking {
        dao.insertBudget(budget("January", 100L, 200L))

        assertEquals("January", dao.getActiveBudget(userId, 150L).first()?.name)
        assertNull(dao.getActiveBudget(userId, 250L).first())
    }

    @Test
    fun insertBudgetCategoriesAndRetrieveThem() = runBlocking {
        val categoryDao = db.categoryDao()
        val categoryId = categoryDao.insertCategory(CategoryEntity(userId = userId, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE))
        val budgetId = dao.insertBudget(budget("January", 100L, 200L))

        dao.insertBudgetCategories(listOf(BudgetCategoryEntity(budgetId = budgetId, categoryId = categoryId, categoryLimit = 50000L)))

        val categories = dao.getBudgetCategories(budgetId).first()
        assertEquals(1, categories.size)
        assertEquals(50000L, categories.single().categoryLimit)
    }
}
