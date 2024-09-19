package ediger.diarynutrition.db


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.getOrAwaitValue
import ediger.diarynutrition.observeForTesting
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

fun Food.assertFood(original: Food) {
    assertThat(this, notNullValue())
    assertThat(this.name, `is`(original.name))
    assertThat(this.cal, `is`(original.cal))
    assertThat(this.prot, `is`(original.prot))
    assertThat(this.fat, `is`(original.fat))
    assertThat(this.carbo, `is`(original.carbo))
    assertThat(this.gi, `is`(original.gi))
    assertThat(this.favorite, `is`(original.favorite))
    assertThat(this.user, `is`(original.user))
    assertThat(this.verified, `is`(original.verified))
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FoodDaoTest {

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
    fun insertFoodAndGetAll() = runBlockingTest {
        database.foodDao().insertFood(FOOD)
        database.foodDao().insertFood(FOOD_2)

        val foodList = database.foodDao().getAllFood().getOrAwaitValue()

        assertThat(foodList.size, `is`(2))

        foodList.find { it.name == FOOD.name }!!.assertFood(FOOD)
        foodList.find { it.name == FOOD_2.name }!!.assertFood(FOOD_2)
    }

    @Test
    fun deleteFoodById() = runBlockingTest {
        database.foodDao().insertFood(FOOD)

        var foodList = database.foodDao().getAllFood().getOrAwaitValue()
        val food = foodList.first()

        val delCount = database.foodDao().deleteFoodById(food.id)

        assertThat(delCount, `is`(1))

        foodList = database.foodDao().getAllFood().getOrAwaitValue()

        assertThat(foodList.size, `is`(0))
    }

    @Test
    fun updateFood() = runBlockingTest {
        database.foodDao().insertFood(FOOD)

        var foodList = database.foodDao().getAllFood().getOrAwaitValue()
        var food = foodList.first().apply {
            favorite = 1
            user = 1
        }

        database.foodDao().updateFood(food)

        foodList = database.foodDao().getAllFood().getOrAwaitValue()
        assertThat(foodList.size, `is`(1))
        food = foodList.first()

        assertThat(food, notNullValue())
        assertThat(food.favorite, `is`(1))
        assertThat(food.user, `is`(1))
    }

    @Test
    fun getFavorite() = runBlockingTest {
        database.foodDao().insertFood(FOOD_2)
        database.foodDao().insertFood(FOOD.apply { favorite = 1 })

        val foodList = database.foodDao().getFavorFood().getOrAwaitValue()
        assertThat(foodList.size, `is`(1))

        foodList.first().assertFood(FOOD.apply { favorite = 1 })
    }

    @Test
    fun getUserFood() = runBlockingTest {
        database.foodDao().insertFood(FOOD_2)
        database.foodDao().insertFood(FOOD.apply { user = 1 })

        val foodList = database.foodDao().getUserFood().getOrAwaitValue()
        assertThat(foodList.size, `is`(1))

        foodList.first().assertFood(FOOD.apply { user = 1 })
    }

    // Paging testing
    //fun insertAndSearchAll() {}
    //fun insertAndSearchFavorite() {}
    //fun insertAndSearchUserFood() {}

    companion object {
        @JvmField
        val FOOD = Food("test food", 1f, 1f, 1f, 1f)
        val FOOD_2 = Food("another one", 2f, 2f, 2f, 2f)
    }
}

