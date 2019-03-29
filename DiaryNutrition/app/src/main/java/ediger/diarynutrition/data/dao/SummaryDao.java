package ediger.diarynutrition.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import ediger.diarynutrition.data.GroupSummary;
import ediger.diarynutrition.data.Summary;

@Dao
public interface SummaryDao {

    /**
     * Summary of macronutrients for specific period
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT " +
            "sum(food.cal / 100 * record.serving) as cal, " +
            "sum(food.prot / 100 * record.serving) as prot, " +
            "sum(food.fat / 100 * record.serving) as fat, " +
            "sum(food.carbo / 100 * record.serving) as carbo " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food._id " +
            "WHERE record_datetime BETWEEN :from AND :to")
    LiveData<Summary> getDaySummary(long from, long to);


    /**
     * Summary of macronutrients for specific period and group
     * @param mealId id of the meal
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT " +
            "sum(food.cal / 100 * record.serving) as cal, " +
            "sum(food.prot / 100 * record.serving) as prot, " +
            "sum(food.fat / 100 * record.serving) as fat, " +
            "sum(food.carbo / 100 * record.serving) as carbo, " +
            "sum(record.serving) as serving " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food._id " +
            "WHERE (record_datetime BETWEEN :from AND :to) AND meal_id = :mealId")
    LiveData<GroupSummary> getGroupSummary(int mealId, long from, long to);


//    /**
//     * Summary of total water amount for specific period
//     * @param from beginning of the period in milliseconds
//     * @param to end of the period in milliseconds
//     */
//    @Query("SELECT sum(amount) FROM water " +
//            "WHERE datetime BETWEEN :from AND :to")
//    LiveData<Summary> getWaterSummary(long from, long to);


}
