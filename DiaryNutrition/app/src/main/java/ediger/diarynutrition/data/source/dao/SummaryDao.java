package ediger.diarynutrition.data.source.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.data.source.entities.Summary;

@Dao
public abstract class SummaryDao {

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
            "INNER JOIN food ON record.food_id = food.id " +
            "WHERE datetime BETWEEN :from AND :to")
    public abstract LiveData<Summary> getDaySummary(long from, long to);

    @Query("SELECT " +
            "sum(food.cal / 100 * record.serving) as cal, " +
            "sum(food.prot / 100 * record.serving) as prot, " +
            "sum(food.fat / 100 * record.serving) as fat, " +
            "sum(food.carbo / 100 * record.serving) as carbo " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food.id " +
            "WHERE datetime BETWEEN :from AND :to")
    abstract Summary getSummary(long from, long to);

    @Transaction
    public List<Summary> getMacroSummary(long from, long to) {
        List<Summary> result = new ArrayList<>();

        Calendar since = Calendar.getInstance();
        since.setTimeInMillis(from);
        Calendar before = Calendar.getInstance();
        before.setTimeInMillis(from);

        while (since.getTimeInMillis() < to) {
            before.set(Calendar.HOUR_OF_DAY, 23);
            before.set(Calendar.MINUTE, 59);
            before.set(Calendar.SECOND, before.getActualMaximum(Calendar.SECOND));
            result.add(getSummary(since.getTimeInMillis(), before.getTimeInMillis()));
            since.add(Calendar.DAY_OF_YEAR, 1);
            before.add(Calendar.DAY_OF_YEAR, 1);
        }

        return result;
    }

//    /**
//     * Summary of total water amount for specific period
//     * @param from beginning of the period in milliseconds
//     * @param to end of the period in milliseconds
//     */
//    @Query("SELECT sum(amount) FROM water " +
//            "WHERE datetime BETWEEN :from AND :to")
//    abstract Water getWaterSummary(long from, long to);


}
