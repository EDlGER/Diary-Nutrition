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
import ediger.diarynutrition.data.dao.MealDao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;


@RunWith(AndroidJUnit4.class)
public class MealDaoTest {

    static final Meal MEAL = new Meal("Breakfast");
    static final Meal MEAL2 = new Meal("Lunch");

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DiaryDatabase mDatabase;
    private MealDao mMealDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DiaryDatabase.class)
                .allowMainThreadQueries()
                .build();
        mMealDao = mDatabase.mealDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertAndGetById() {
        mMealDao.insertMeal(MEAL);
        Meal meal = mMealDao.getMeal(1);
        assertMeal(meal, MEAL.getName());
    }

    @Test
    public void insertAndUpdate() {
        mMealDao.insertMeal(MEAL);
        Meal meal = mMealDao.getMeal(1);
        meal.setName("Name");

        mMealDao.updateMeal(meal);
        meal = mMealDao.getMeal(1);
        assertMeal(meal, "Name");
    }

    @Test
    public void insertAndDeleteById() {
        mMealDao.insertMeal(MEAL);
        mMealDao.deleteMealById(1);
        Meal meal = mMealDao.getMeal(1);
        assertNull(meal);
    }

    @Test
    public void insertAndGetAll() throws Exception {
        mMealDao.insertMeal(MEAL);
        mMealDao.insertMeal(MEAL2);

        List<Meal> meals = LiveDataTestUtil.getValue(mMealDao.getMeals());
        assertThat(meals.size(), is(2));

        assertMeal(meals.get(1), MEAL2.getName());
    }

    private void assertMeal(Meal meal, String name) {
        assertThat(meal, notNullValue());
        assertThat(meal.getName(), is(name));
    }

}
