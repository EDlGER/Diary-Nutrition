package ediger.diarynutrition.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.db.FoodDaoTest.Companion.FOOD
import ediger.diarynutrition.db.MealDaoTest.Companion.MEAL
import ediger.diarynutrition.db.RecordDaoTest.Companion.RECORD
import ediger.diarynutrition.db.RecordDaoTest.Companion.RECORD_2
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

fun Summary.assertSummary(original: Summary) {
    assertThat(this, notNullValue())
    assertThat(this.cal, `is`(original.cal))
    assertThat(this.prot, `is`(original.prot))
    assertThat(this.fat, `is`(original.fat))
    assertThat(this.carbo, `is`(original.carbo))
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SummaryDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DiaryDatabase

    @Before
    fun initDb() = runBlockingTest {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                DiaryDatabase::class.java
        ).build()

        database.mealDao().insertMeal(MEAL)
        database.foodDao().insertFood(FOOD)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getSummaryForPeriod() = runBlockingTest {
        //One day ~ 87 000 000 millis
        val record = Record(1, 1, 150, 100000000)
        database.recordDao().populateRecords(listOf(RECORD, RECORD_2, record))

        val summaryList = database.summaryDao().getMacroSummary(0, 200000000)
        assertThat(summaryList.size, `is`(3))

        summaryList.find { it.cal == (FOOD.cal * record.serving / 100) }.also { secondDaySummary ->
            assertThat(secondDaySummary, notNullValue())
        }

    }

}