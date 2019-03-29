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

    static final Food FOOD = new Food("test food", 1, 1, 1, 1);

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

    @Test
    public void insertAndUpdate() {
        mFoodDao.insertFood(FOOD);
        Food food = mFoodDao.getFood(1);
        food.setFavor(1);
        food.setUsr(1);
        mFoodDao.updateFood(food);

        food = mFoodDao.getFood(1);

        assertFood(food, 1, FOOD.getFoodName(), FOOD.getCal(),
                FOOD.getProt(), FOOD.getFat(), FOOD.getCarbo());
        assertThat(food.getFavor(), is(1));
        assertThat(food.getUsr(), is(1));
    }

    @Test
    public void insertAndGetFavor() throws Exception {
        Food food = new Food(FOOD.getFoodName(), FOOD.getCal(),
                FOOD.getProt(), FOOD.getFat(), FOOD.getCarbo());
        food.setFavor(1);
        mFoodDao.insertFood(food);
        mFoodDao.insertFood(food);

        List<Food> foods = LiveDataTestUtil.getValue(mFoodDao.getFavorFood());
        assertThat(foods.size(), is(2));

        food = foods.get(0);

        assertFood(food, 1, FOOD.getFoodName(), FOOD.getCal(),
                FOOD.getProt(), FOOD.getFat(), FOOD.getCarbo());
        assertThat(food.getFavor(), is(1));
    }

    @Test
    public void insertAndGetUsr() throws Exception {
        Food food = new Food(FOOD.getFoodName(), FOOD.getCal(),
                FOOD.getProt(), FOOD.getFat(), FOOD.getCarbo());
        food.setUsr(1);
        mFoodDao.insertFood(food);
        mFoodDao.insertFood(food);

        List<Food> foods = LiveDataTestUtil.getValue(mFoodDao.getUserFood());
        assertThat(foods.size(), is(2));

        food = foods.get(0);

        assertFood(food, 1, FOOD.getFoodName(), FOOD.getCal(),
                FOOD.getProt(), FOOD.getFat(), FOOD.getCarbo());
        assertThat(food.getFavor(), is(0));
        assertThat(food.getUsr(), is(1));
    }

    @Test
    public void insertAndSearchAll() throws Exception {
        mFoodDao.insertFood(FOOD);
        Food food = new Food("food", 1, 1, 1, 1);
        mFoodDao.insertFood(food);

        List<Food> foods = LiveDataTestUtil.getValue(mFoodDao.searchAllFood("%food%"));
        assertThat(foods.size(), is(2));

        foods = LiveDataTestUtil.getValue(mFoodDao.searchAllFood("%test%"));
        assertThat(foods.size(), is(1));

        assertThat(foods.get(0).getFoodName(), is("test food"));
    }

    @Test
    public void insertAndSearchFavor() throws Exception {
        Food food = new Food("test food", 1, 1, 1, 1);
        food.setFavor(1);
        mFoodDao.insertFood(food);
        food.setFoodName("food");
        mFoodDao.insertFood(food);

        List<Food> foods = LiveDataTestUtil.getValue(mFoodDao.searchFavorFood("%food%"));
        assertThat(foods.size(), is(2));

        foods = LiveDataTestUtil.getValue(mFoodDao.searchFavorFood("%test%"));
        assertThat(foods.size(), is(1));

        assertThat(foods.get(0).getFavor(), is(1));
        assertThat(foods.get(0).getFoodName(), is("test food"));
    }

    static void assertFood(Food food, int id, String foodName,
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
