package ediger.diarynutrition.data.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;
import ediger.diarynutrition.data.Record;
import ediger.diarynutrition.data.RecordWithFoodAndMeal;

@Dao
public interface RecordDao {

//    /**
//     * Get children for specific meal group and period
//     * @param mealId id of the meal
//     * @param from beginning of the period in milliseconds
//     * @param to end of the period in milliseconds
//     * @return Children for specific meal group
//     */

//    @Query("SELECT " +
//            "record._id, " +
//            "record.serving, " +
//            "record.record_datetime, " +
//            "record.food_id, " +
//            "record.meal_id, " +
//            "food.food_name, " +
//            "(food.cal / 100 * record.serving) as cal, " +
//            "(food.prot / 100 * record.serving) as prot, " +
//            "(food.fat / 100 * record.serving) as fat, " +
//            "(food.carbo / 100 * record.serving) as carbo, " +
//            "meal.name " +
//            "FROM record " +
//            "INNER JOIN food ON record.food_id = food._id " +
//            "INNER JOIN meal ON record.meal_id = meal._id " +
//            "WHERE (record_datetime BETWEEN :from AND :to) AND meal._id = :mealId " +
//            "ORDER BY record_datetime ASC")
//    LiveData<List<RecordWithFoodAndMeal>> loadRecords(int mealId, long from, long to);

    @Query("SELECT * FROM record WHERE _id = :id")
    Record getRecord(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRecord(Record record);

    @Query("DELETE FROM record WHERE _id = :id")
    int delRecordById(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateRecord(Record record);

}
