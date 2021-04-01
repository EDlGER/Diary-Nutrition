package ediger.diarynutrition.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Water
import ediger.diarynutrition.getOrAwaitValue
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

fun Water.assertWater(original: Water) {
    assertThat(this, notNullValue())
    assertThat(this.amount, `is`(original.amount))
    assertThat(this.datetime, `is`(original.datetime))
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class WaterDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DiaryDatabase

    @Before
    fun initDb() = runBlockingTest {
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
    fun populateAndGetRecords() = runBlockingTest {
        val water = Water(1000, 222222)
        database.waterDao().populateWater(listOf(WATER, water))
        val waterList = database.waterDao().getWaterList(0, 300000).getOrAwaitValue()
        assertThat(waterList.size, `is`(2))

        waterList.find { it.datetime == WATER.datetime }!!.assertWater(WATER)
        waterList.find { it.datetime == water.datetime }!!.assertWater(water)
    }

    @Test
    fun insertAndDeleteById() = runBlockingTest {
        database.waterDao().insertWater(WATER)

        var waterList = database.waterDao().getWater()
        assertThat(waterList.size, `is`(1))

        database.waterDao().deleteWaterById(1)

        waterList = database.waterDao().getWater()
        assertThat(waterList.size, `is`(0))
    }

    @Test
    fun insertAndGetAmountSum() = runBlockingTest {
        val water = Water(1000, 222222)
        database.waterDao().populateWater(listOf(WATER, water))

        val waterSum = database.waterDao().getWaterSum(0, 300000).getOrAwaitValue()
        assertThat(waterSum, notNullValue())
        assertThat(waterSum.amount, `is`(WATER.amount + water.amount))
    }

    @Test
    fun getAmountSum_empty() {
        val waterSum = database.waterDao().getWaterSum(0, 300000).getOrAwaitValue()

        assertThat(waterSum, notNullValue())
        assertThat(waterSum.amount, `is`(0))
    }

    companion object {
        val WATER = Water(250, 111111)
    }
 }
