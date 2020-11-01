package ediger.diarynutrition.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import ediger.diarynutrition.data.source.DiaryDatabase;
import ediger.diarynutrition.data.source.entities.Food;
import ediger.diarynutrition.data.source.entities.Meal;
import ediger.diarynutrition.data.source.entities.MealAndRecords;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.RecordAndFood;
import ediger.diarynutrition.data.source.dao.RecordDao;

import static ediger.diarynutrition.db.FoodDaoTest.FOOD;
import static ediger.diarynutrition.db.MealDaoTest.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class RecordDaoTest {

    static final Record RECORD = new Record(1, 1, 250, 111111);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DiaryDatabase mDatabase;
    private RecordDao mRecordDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mRecordDao = mDatabase.recordDao();

        //mDatabase.foodDao().insertFood(FOOD);
        mDatabase.mealDao().insertMeal(MealDaoTest.MEAL);
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertAndGetById() {
        mRecordDao.insertRecord(RECORD);

        Record record = mRecordDao.getRecord(1);

        assertRecord(record, RECORD.getMealId(), RECORD.getFoodId(), RECORD.getServing(), RECORD.getDatetime());

    }

    @Test
    public void insertAndDeleteById() {
        mRecordDao.insertRecord(RECORD);
        mRecordDao.delRecordById(1);

        Record record = mRecordDao.getRecord(1);
        assertNull(record);
    }

    @Test
    public void insertAndUpdate() {
        mRecordDao.insertRecord(RECORD);
        mDatabase.mealDao().insertMeal(new Meal("Lunch", 0));

        Record record = mRecordDao.getRecord(1);
        record.setMealId(2);

        mRecordDao.updateRecord(record);
        record = mRecordDao.getRecord(1);
        assertRecord(record, 2, RECORD.getFoodId(), RECORD.getServing(), RECORD.getDatetime());
    }
    // TODO: Test it!
    @Test
    public void insertFewAndGetAll() {
        mRecordDao.insertRecord(RECORD);
        mRecordDao.insertRecord(RECORD);
        mRecordDao.insertRecord(RECORD);

        List<MealAndRecords> mealAndRecords = mRecordDao.getMealsAndRecords(0, 200000);

        assertThat(mealAndRecords.size(), is(1));
        assertMeal(MEAL, mealAndRecords.get(0).getMeal().getName());

        List<RecordAndFood> records = mealAndRecords.get(0).getRecords();
        assertThat(records.size(), is(3));

        Record record = records.get(0).getRecord();
        assertRecord(RECORD, record.getMealId(), record.getFoodId(), record.getServing(), record.getDatetime());

        Food food = records.get(0).getFood();
        assertThat(food.getName(), is("test food"));
        assertThat(food.getCal(), is(FOOD.getCal() / 100 * record.getServing()));

    }

    private void assertRecord(Record record, int mealId, int foodId, int serving, long datetime) {
        assertThat(record, notNullValue());
        assertThat(record.getMealId(), is(mealId));
        assertThat(record.getFoodId(), is(foodId));
        assertThat(record.getServing(), is(serving));
        assertThat(record.getDatetime(), is(datetime));
    }

}
