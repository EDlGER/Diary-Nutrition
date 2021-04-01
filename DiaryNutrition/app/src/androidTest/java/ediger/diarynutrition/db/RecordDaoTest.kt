package ediger.diarynutrition.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.db.FoodDaoTest.Companion.FOOD
import ediger.diarynutrition.db.MealDaoTest.Companion.MEAL
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

fun Record.assertRecord(original: Record) {
    assertThat(this, notNullValue())
    assertThat(this.mealId, `is`(original.mealId))
    assertThat(this.foodId, `is`(original.foodId))
    assertThat(this.serving, `is`(original.serving))
    assertThat(this.datetime, `is`(original.datetime))
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RecordDaoTest {

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
    fun insertAndGetById() = runBlockingTest {
        database.recordDao().insertRecord(RECORD)

        val record = database.recordDao().getRecord(1)
        record.assertRecord(RECORD)
    }

    @Test
    fun insertAndDeleteById() = runBlockingTest {
        database.recordDao().insertRecord(RECORD)

        var recordList = database.recordDao().getRecords()
        assertThat(recordList.size, `is`(1))

        database.recordDao().delRecordById(1)

        recordList = database.recordDao().getRecords()
        assertThat(recordList.size, `is`(0))
    }

    @Test
    fun insertAndUpdate() = runBlockingTest {
        database.recordDao().insertRecord(RECORD)

        var record = database.recordDao().getRecord(1).apply { serving = 400 }

        database.recordDao().updateRecord(record)

        record = database.recordDao().getRecord(1)
        record.assertRecord(RECORD.apply { serving = 400 })
    }

    @Test
    fun insertFewAndGetAll() = runBlockingTest {
        database.recordDao().insertRecord(Record(1, 1, 100, 333333))
        database.recordDao().insertRecord(RECORD)
        database.recordDao().insertRecord(RECORD_2)

        val recordList = database.recordDao().getMealsAndRecords(0, 300000)
        assertThat(
                recordList.flatMap { it.records!! }.size,
                `is`(2)
        )

        recordList.flatMap { it.records!! }
                .map { it.record }
                .find { it!!.datetime == RECORD.datetime }!!
                .assertRecord(RECORD)

        recordList.flatMap { it.records!! }
                .map { it.record }
                .find { it!!.datetime == RECORD_2.datetime }!!
                .assertRecord(RECORD_2)
    }

    @Test
    fun populateRecords() = runBlockingTest {
        database.recordDao().populateRecords(listOf(RECORD, RECORD_2))

        val recordList = database.recordDao().getRecords()
        assertThat(recordList.size, `is`(2))

        recordList.find { it.datetime == RECORD.datetime }!!.assertRecord(RECORD)
        recordList.find { it.datetime == RECORD_2.datetime }!!.assertRecord(RECORD_2)
    }

    companion object {
        val RECORD = Record(1, 1, 250, 111111)
        val RECORD_2 = Record(1, 1, 100, 222222)
    }
}