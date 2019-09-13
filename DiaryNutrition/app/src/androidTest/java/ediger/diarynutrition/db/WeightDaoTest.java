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
import ediger.diarynutrition.data.source.DiaryDatabase;
import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.data.source.dao.WeightDao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class WeightDaoTest {

    static final Weight WEIGHT = new Weight(75, 111111);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DiaryDatabase mDatabase;
    private WeightDao mWeightDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mWeightDao = mDatabase.weightDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertAndGet() throws Exception {
        mWeightDao.insertWeight(WEIGHT);

        List<Weight> weights = LiveDataTestUtil.getValue(mWeightDao.getWeight(0, 200000));
        assertThat(weights.size(), is(1));
        assertWeight(weights.get(0), 1, WEIGHT.getAmount(), WEIGHT.getDatetime());
    }

    @Test
    public void insertAndLoadDesc() throws Exception {
        mWeightDao.insertWeight(new Weight(80, 100000));
        mWeightDao.insertWeight(WEIGHT);

        List<Weight> weights = LiveDataTestUtil.getValue(mWeightDao.loadWeight());
        assertThat(weights.size(), is(2));

        assertWeight(weights.get(0), 2, WEIGHT.getAmount(), WEIGHT.getDatetime());
    }

    @Test
    public void insertAndDeleteById() throws Exception {
        mWeightDao.insertWeight(WEIGHT);
        mWeightDao.deleteWeightById(1);

        List<Weight> weights = LiveDataTestUtil.getValue(mWeightDao.loadWeight());
        assertThat(weights.size(), is(0));
    }

    @Test
    public void insertAndUpdate() throws Exception {
        mWeightDao.insertWeight(WEIGHT);
        List<Weight> weights = LiveDataTestUtil.getValue(mWeightDao.getWeight(0, 200000));
        assertThat(weights.size(), is(1));

        Weight weight = weights.get(0);
        weight.setAmount(100);
        mWeightDao.updateWeight(weight);

        weights = LiveDataTestUtil.getValue(mWeightDao.getWeight(0, 200000));
        assertWeight(weights.get(0), 1, 100, WEIGHT.getDatetime());
    }

    private void assertWeight(Weight weight, int id, float amount, long datetime) {
        assertThat(weight, notNullValue());
        assertThat(weight.getId(), is(id));
        assertThat(weight.getAmount(), is(amount));
        assertThat(weight.getDatetime(), is(datetime));
    }

}
