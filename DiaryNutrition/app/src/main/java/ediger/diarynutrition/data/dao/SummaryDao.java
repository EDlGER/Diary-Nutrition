package ediger.diarynutrition.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import ediger.diarynutrition.data.Summary;

@Dao
public interface SummaryDao {

    /**
     * It is intended to use in summary
     * 86356262 = 24 hours
     * @param date beginning of the day in milliseconds
     * @return Summary of the day
     */
    @Query("SELECT " +
            "sum(food.cal / 100 * record.serving) as cal, " +
            "sum(food.prot / 100 * record.serving) as prot, " +
            "sum(food.fat / 100 * record.serving) as fat, " +
            "sum(food.carbo / 100 * record.serving) as carbo " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food._id " +
            "WHERE record_datetime BETWEEN :date AND (:date + 86356262)")
    LiveData<Summary> getDayData(long date);

    /**
     *
     * @param date beginning of the day in milliseconds
     * @param mealId
     * @return Summary of records depending on mealId
     */
    @Query("SELECT " +
            "sum(food.cal / 100 * record.serving) as cal, " +
            "sum(food.prot / 100 * record.serving) as prot, " +
            "sum(food.fat / 100 * record.serving) as fat, " +
            "sum(food.carbo / 100 * record.serving) as carbo, " +
            "sum(record.serving) as serving " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food._id " +
            "WHERE (record_datetime BETWEEN :date AND (:date + 86356262)) AND meal_id = :mealId")
    LiveData<Summary> getGroupData(long date, int mealId);

}
