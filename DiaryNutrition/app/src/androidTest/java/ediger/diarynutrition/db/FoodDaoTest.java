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
import ediger.diarynutrition.data.Food;
import ediger.diarynutrition.data.dao.FoodDao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AndroidJUnit4.class)
public class FoodDaoTest {

    private static final Food FOOD= new Food("test_food", 100, 5, 5, 5);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DiaryDatabase mDatabase;

    private FoodDao mFoodDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mFoodDao = mDatabase.foodDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertFoodAndGetAll() throws Exception {
        mFoodDao.insertFood(FOOD);

        List<Food> foods = LiveDataTestUtil.getValue(mFoodDao.getAllFood());

        assertThat(foods.size(), is(1));

        Food food = foods.get(0);

        assertFood(food, foods.size(), FOOD.getFoodName(), FOOD.getCal(), FOOD.getProt(),
                FOOD.getFat(), FOOD.getCarbo());
    }

    @Test
    public void deleteFoodById() throws Exception {
        mFoodDao.insertFood(FOOD);

        List<Food> foods = LiveDataTestUtil.getValue(mFoodDao.getAllFood());

        Food food = foods.get(0);

        int delCount = mFoodDao.deleteFoodById(food.getId());

        assertThat(delCount, is(1));

        foods = LiveDataTestUtil.getValue(mFoodDao.getAllFood());

        assertThat(foods.size(), is(0));

    }

    private void assertFood(Food food, int id, String foodName,
                            float cal, float prot, float fat, float carbo) {
        assertThat(food, notNullValue());
        assertThat(food.getId(), is(id));
        assertThat(food.getFoodName(), is(foodName));
        assertThat(food.getCal(), is(cal));
        assertThat(food.getProt(), is(prot));
        assertThat(food.getFat(), is(fat));
        assertThat(food.getCarbo(), is(carbo));
    }

}
