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
import ediger.diarynutrition.data.Water;
import ediger.diarynutrition.data.dao.WaterDao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class WaterDaoTest {

    static final Water WATER = new Water(250, 111111);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DiaryDatabase mDatabase;
    private WaterDao mWaterDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mWaterDao = mDatabase.waterDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertAndDeleteById() throws Exception {
        mWaterDao.insertWater(WATER);
        mWaterDao.deleteWaterById(1);

        List<Water> waterList = LiveDataTestUtil.getValue(mWaterDao.loadWater(0, 200000));
        assertThat(waterList.size(), is(0));
    }

    @Test
    public void insertAndGetAll() throws Exception {
        mWaterDao.insertWater(WATER);
        mWaterDao.insertWater(new Water(100, 111111));

        List<Water> waterList = LiveDataTestUtil.getValue(mWaterDao.loadWater(0, 200000));
        assertThat(waterList.size(), is(2));
        assertWater(waterList.get(0), 1, WATER.getAmount(), WATER.getDatetime());
    }

    @Test
    public void insertAndGetAmountSum() throws Exception {
        mWaterDao.insertWater(WATER);
        mWaterDao.insertWater(new Water(100, 111111));

        Water sumWater = LiveDataTestUtil.getValue(mWaterDao.getWaterSum(0, 200000));
        assertThat(sumWater, notNullValue());
        assertThat(sumWater.getAmount(), is(350));
    }

    @Test
    public void getEmptyAmountSum() throws Exception {
        Water sumWater = LiveDataTestUtil.getValue(mWaterDao.getWaterSum(0, 200000));

        assertThat(sumWater, notNullValue());
        assertThat(sumWater.getAmount(), is(0));
    }

    private void assertWater(Water water, int id, int amount, long datetime) {
        assertThat(water, notNullValue());
        assertThat(water.getId(), is(id));
        assertThat(water.getAmount(), is(amount));
        assertThat(water.getDatetime(), is(datetime));
    }

}
