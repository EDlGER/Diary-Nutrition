package ediger.diarynutrition.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Weight
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

fun Weight.assertWeight(original: Weight) {
    assertThat(this, notNullValue())
    assertThat(this.amount, `is`(original.amount))
    assertThat(this.datetime, `is`(original.datetime))
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class WeightDaoTest {

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
    fun insertAndGet() = runBlockingTest {
        database.weightDao().insertWeight(WEIGHT)

        val weightList = database.weightDao().getWeight()
        assertThat(weightList.size, `is`(1))

        weightList.first().assertWeight(WEIGHT)
    }

    @Test
    fun populateAndLoad() = runBlockingTest {
        val weight = Weight(80f, 222222)
        database.weightDao().populateWeight(listOf(WEIGHT, weight))

        val weightList = database.weightDao().loadWeight().getOrAwaitValue()
        assertThat(weightList.size, `is`(2))

        weightList.find { it.amount == WEIGHT.amount }!!.assertWeight(WEIGHT)
        weightList.find { it.amount == weight.amount }!!.assertWeight(weight)
    }

    @Test
    fun deleteById() = runBlockingTest {
        database.weightDao().insertWeight(WEIGHT)

        var weightList = database.weightDao().getWeight()
        assertThat(weightList.size, `is`(1))

        database.weightDao().deleteWeightById(1)

        weightList = database.weightDao().getWeight()
        assertThat(weightList.size, `is`(0))
    }

    @Test
    fun updateWeight() = runBlockingTest {
        database.weightDao().insertWeight(WEIGHT)

        val weight = database.weightDao().getWeight().first().apply {
            amount = 800f
        }

        database.weightDao().updateWeight(weight)

        database.weightDao().getWeight().first().apply {
            assertWeight(WEIGHT.apply { amount = 800f })
        }
    }

    companion object {
        val WEIGHT = Weight(75f, 111111)
    }
}