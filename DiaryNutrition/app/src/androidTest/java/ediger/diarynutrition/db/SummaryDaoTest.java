package ediger.diarynutrition.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import ediger.diarynutrition.LiveDataTestUtil;
import ediger.diarynutrition.data.DiaryDatabase;
import ediger.diarynutrition.data.GroupSummary;
import ediger.diarynutrition.data.Record;
import ediger.diarynutrition.data.Summary;
import ediger.diarynutrition.data.dao.SummaryDao;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SummaryDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DiaryDatabase mDatabase;
    private SummaryDao mSummaryDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mSummaryDao = mDatabase.summaryDao();

        addInitData();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void getSummaryForPeriod() throws Exception {
        Summary summary = LiveDataTestUtil.getValue(mSummaryDao.getDaySummary(0, 200000));

        assertSummary(summary, 7.5F, 7.5F, 7.5F, 7.5F);
    }

    @Test
    public void getGroupSummary() throws Exception {
        GroupSummary summary = LiveDataTestUtil.getValue(mSummaryDao.getGroupSummary(2,
                0, 200000));
        assertNull(summary.summary);
        assertThat(summary.getServing(), is(0));

        mDatabase.mealDao().insertMeal(MealDaoTest.MEAL2);
        Record record = mDatabase.recordDao().getRecord(1);
        record.setMealId(2);
        mDatabase.recordDao().updateRecord(record);

        summary = LiveDataTestUtil.getValue(mSummaryDao.getGroupSummary(2,
                0, 200000));

        assertSummary(summary.summary, 2.5F, 2.5F, 2.5F, 2.5F);
        assertThat(summary.getServing(), is(250));

    }

    private void addInitData() {
        mDatabase.foodDao().insertFood(FoodDaoTest.FOOD);
        mDatabase.mealDao().insertMeal(MealDaoTest.MEAL);

        mDatabase.recordDao().insertRecord(RecordDaoTest.RECORD);
        mDatabase.recordDao().insertRecord(RecordDaoTest.RECORD);
        mDatabase.recordDao().insertRecord(RecordDaoTest.RECORD);
    }

    private void assertSummary(Summary summary, float cal, float prot, float fat, float carbo) {
        assertThat(summary, notNullValue());
        assertThat(summary.getCal(), is(cal));
        assertThat(summary.getProt(), is(prot));
        assertThat(summary.getFat(), is(fat));
        assertThat(summary.getCarbo(), is(carbo));
    }

}
