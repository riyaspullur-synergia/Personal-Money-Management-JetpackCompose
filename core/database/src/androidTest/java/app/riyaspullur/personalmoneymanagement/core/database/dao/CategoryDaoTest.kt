package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CategoryDao
    private var userId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.categoryDao()
        userId = db.userDao().insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllActiveCategoriesExcludesArchived() = runBlocking {
        dao.insertCategory(CategoryEntity(userId = userId, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE))
        dao.insertCategory(CategoryEntity(userId = userId, name = "Old", icon = null, color = 0, type = TransactionType.EXPENSE, isArchived = true))

        val categories = dao.getAllActiveCategories(userId).first()

        assertEquals(listOf("Food"), categories.map { it.name })
    }

    @Test
    fun getActiveCategoriesByTypeFiltersByType() = runBlocking {
        dao.insertCategory(CategoryEntity(userId = userId, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE))
        dao.insertCategory(CategoryEntity(userId = userId, name = "Salary", icon = null, color = 0, type = TransactionType.INCOME))

        val expenseCategories = dao.getActiveCategoriesByType(userId, TransactionType.EXPENSE).first()

        assertEquals(listOf("Food"), expenseCategories.map { it.name })
    }

    @Test
    fun updateCategoryPersistsChanges() = runBlocking {
        val id = dao.insertCategory(CategoryEntity(userId = userId, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE))
        val category = dao.getAllActiveCategories(userId).first().single()

        dao.updateCategory(category.copy(name = "Groceries"))

        assertEquals("Groceries", dao.getAllActiveCategories(userId).first().single { it.id == id }.name)
    }
}
