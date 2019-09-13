package ediger.diarynutrition.data.source.dao;

import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import ediger.diarynutrition.data.source.entities.Meal;
import ediger.diarynutrition.data.source.entities.MealAndRecords;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.RecordAndFood;

@Dao
public abstract class RecordDao {

    @Query("SELECT * FROM meal")
    abstract List<Meal> getMeals();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT " +
            "record.*, " +
            "food.food_name as f_food_name, " +
            "(food.cal / 100 * record.serving) as f_cal, " +
            "(food.prot / 100 * record.serving) as f_prot, " +
            "(food.fat / 100 * record.serving) as f_fat, " +
            "(food.carbo / 100 * record.serving) as f_carbo " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food.id " +
            "WHERE meal_id = :mealId AND record_datetime BETWEEN :from AND :to")
    abstract List<RecordAndFood> getRecordsAndFoods(long mealId, long from, long to);

    @Transaction
    public List<MealAndRecords> getMealsAndRecords(long from, long to) {
        //MutableLiveData<List<MealAndRecords>> result = new MutableLiveData<>();

        List<MealAndRecords> mealAndRecordsList = new ArrayList<>();
        List<Meal> meals = getMeals();
        for (Meal meal : meals) {
            MealAndRecords mealAndRecords = new MealAndRecords();
            mealAndRecords.meal = meal;
            mealAndRecords.records = getRecordsAndFoods(meal.getId(), from, to);
            mealAndRecordsList.add(mealAndRecords);
        }
        //result.postValue(mealAndRecordsList);

        return mealAndRecordsList;
    }

    @Query("SELECT * FROM record WHERE id = :id")
    public abstract Record getRecord(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertRecord(Record record);

    @Query("DELETE FROM record WHERE id = :id")
    public abstract int delRecordById(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateRecord(Record record);

}