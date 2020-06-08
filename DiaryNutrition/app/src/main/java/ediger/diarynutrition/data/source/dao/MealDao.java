package ediger.diarynutrition.data.source.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import ediger.diarynutrition.data.source.entities.Meal;

@Dao
public interface MealDao {

    @Query("SELECT * FROM meal ORDER BY id ASC")
    LiveData<List<Meal>> getMeals();

    @Query("SELECT * FROM meal WHERE id = :id")
    Meal getMeal(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMeal(Meal meal);

    @Query("DELETE FROM meal WHERE id = :id")
    int deleteMealById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMeal(Meal meal);

    //get and set user field

}
