package ediger.diarynutrition.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

fun Meal.assertMeal(original: Meal) {
    assertThat(this, notNullValue())
    assertThat(this.name, `is`(original.name))
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MealDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DiaryDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                DiaryDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetById() = runBlockingTest {
        database.mealDao().insertMeal(MEAL)
        val meal = database.mealDao().getMeal(1)
        meal.assertMeal(MEAL)
    }

    @Test
    fun insertAndUpdate() = runBlockingTest {
        database.mealDao().insertMeal(MEAL)
        var meal = database.mealDao().getMeal(1).apply { name = "Different" }

        database.mealDao().updateMeal(meal)
        meal = database.mealDao().getMeal(1)

        meal.assertMeal(MEAL.apply { name = "Different" })
    }

    @Test
    fun insertAndDeleteById() = runBlockingTest {
        database.mealDao().insertMeal(MEAL)

        var mealList = database.mealDao().getMeals()
        assertThat(mealList.size, `is`(1))

        database.mealDao().deleteMealById(1)

        mealList = database.mealDao().getMeals()
        assertThat(mealList.size, `is`(0))
    }

    @Test
    fun insertAndGetAll() = runBlockingTest {
        database.mealDao().insertMeal(MEAL)
        database.mealDao().insertMeal(MEAL_2)

        val mealList = database.mealDao().getMeals()
        assertThat(mealList.size, `is`(2))

        mealList.find { it.name == MEAL.name }!!.assertMeal(MEAL)
        mealList.find { it.name == MEAL_2.name }!!.assertMeal(MEAL_2)
    }

    @Test
    fun populateMeals() = runBlockingTest {
        database.mealDao().populateMeals(listOf(MEAL, MEAL_2))

        val mealList = database.mealDao().getMeals()
        assertThat(mealList.size, `is`(2))

        mealList.find { it.name == MEAL.name }!!.assertMeal(MEAL)
        mealList.find { it.name == MEAL_2.name }!!.assertMeal(MEAL_2)
    }

    companion object {
        val MEAL = Meal("Breakfast", 0)
        val MEAL_2 = Meal("Lunch", 0)
    }
}