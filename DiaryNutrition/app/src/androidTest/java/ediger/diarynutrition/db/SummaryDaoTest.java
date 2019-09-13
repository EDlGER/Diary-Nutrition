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

import java.util.List;

import ediger.diarynutrition.LiveDataTestUtil;
import ediger.diarynutrition.data.source.DiaryDatabase;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.data.source.dao.SummaryDao;

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

    //One day ~ 87 000 000 millis
    @Test
    public void getSummaryForPeriod() {
        mDatabase.recordDao().insertRecord(
                new Record(1, 1, 200, 100_000_000));
        List<Summary> summary = mSummaryDao.getMacroSummary(0, 200_000_000);

        assertThat(summary, notNullValue());
        assertThat(summary.size(), is(3));

        Summary firstDaySummary = summary.get(0);
        Summary secondDaySummary = summary.get(1);

        assertSummary(firstDaySummary, 7.5F, 7.5F, 7.5F, 7.5F);
        assertSummary(secondDaySummary, 2, 2,2,2);
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
