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
import androidx.test.runner.AndroidJUnit4;
import ediger.diarynutrition.LiveDataTestUtil;
import ediger.diarynutrition.data.DiaryDatabase;
import ediger.diarynutrition.data.Meal;
import ediger.diarynutrition.data.Record;
import ediger.diarynutrition.data.RecordWithFoodAndMeal;
import ediger.diarynutrition.data.dao.RecordDao;

import static ediger.diarynutrition.db.FoodDaoTest.FOOD;
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
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mRecordDao = mDatabase.recordDao();

        mDatabase.foodDao().insertFood(FOOD);
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
        mDatabase.mealDao().insertMeal(new Meal("Lunch"));

        Record record = mRecordDao.getRecord(1);
        record.setMealId(2);

        mRecordDao.updateRecord(record);
        record = mRecordDao.getRecord(1);
        assertRecord(record, 2, RECORD.getFoodId(), RECORD.getServing(), RECORD.getDatetime());
    }

    @Test
    public void insertFewAndLoadAll() throws Exception {
        mRecordDao.insertRecord(RECORD);
        mRecordDao.insertRecord(RECORD);
        mRecordDao.insertRecord(RECORD);

        List<RecordWithFoodAndMeal> records = LiveDataTestUtil.getValue(mRecordDao
                .loadRecords(RECORD.getMealId(), 0, 200000));

        assertThat(records.size(), is(3));

        Record record = records.get(1).record;
        assertRecord(record, RECORD.getMealId(), RECORD.getFoodId(), RECORD.getServing(), RECORD.getDatetime());

        float testValue = FOOD.getCal() / 100 * RECORD.getServing();

        //Id field doesn't change
        FoodDaoTest.assertFood(records.get(1).food, 0, FOOD.getFoodName(),
                testValue, testValue, testValue, testValue);
    }

    private void assertRecord(Record record, int mealId, int foodId, int serving, long datetime) {
        assertThat(record, notNullValue());
        assertThat(record.getMealId(), is(mealId));
        assertThat(record.getFoodId(), is(foodId));
        assertThat(record.getServing(), is(serving));
        assertThat(record.getDatetime(), is(datetime));
    }

}
