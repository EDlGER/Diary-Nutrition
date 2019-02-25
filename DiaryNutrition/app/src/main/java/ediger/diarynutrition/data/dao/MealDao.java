package ediger.diarynutrition.data.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import ediger.diarynutrition.data.Meal;

@Dao
public interface MealDao {

    @Query("SELECT * FROM meal ORDER BY _id ASC")
    LiveData<List<Meal>> getMeals();

    @Query("SELECT * FROM meal WHERE _id = :id")
    Meal getMeal(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMeal(Meal meal);

    @Query("DELETE FROM meal WHERE _id = :id")
    int deleteMealById(int id);

}
