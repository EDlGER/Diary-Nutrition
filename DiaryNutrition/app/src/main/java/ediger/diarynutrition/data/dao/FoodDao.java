package ediger.diarynutrition.data.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import ediger.diarynutrition.data.Food;

@Dao
public interface FoodDao {

    @Query("SELECT * FROM food ORDER BY food_name ASC")
    LiveData<List<Food>> getAllFood();

    @Query("SELECT * FROM food WHERE favor = 1 ORDER BY food_name ASC")
    LiveData<List<Food>> getFavorFood();

    @Query("SELECT * FROM food WHERE usr = 1 ORDER BY food_name ASC")
    LiveData<List<Food>> getUserFood();

    @Query("SELECT * FROM food WHERE _id = :id")
    Food getFood(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFood(Food food);

    @Update
    void updateFood(Food food);

    @Query("DELETE FROM food WHERE _id = :id")
    int deleteFoodById(int id);



    //TODO Find food by food_name

}
