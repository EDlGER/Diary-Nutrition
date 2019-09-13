package ediger.diarynutrition.data.source.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import ediger.diarynutrition.data.source.entities.Water;

@Dao
public abstract class WaterDao {

    /**
     * Get water records for specific period
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT * FROM water WHERE datetime BETWEEN :from AND :to ORDER BY datetime ASC")
    public abstract LiveData<List<Water>> getWaterList(long from, long to);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertWater(Water water);

    @Query("DELETE FROM water WHERE id = :id")
    public abstract int deleteWaterById(int id);

    @Query("SELECT " +
            "id, " +
            "sum(amount) AS amount, " +
            "datetime " +
            "FROM water WHERE datetime BETWEEN :from AND :to")
    public abstract LiveData<Water> getWaterSum(long from, long to);

    @Query("SELECT " +
            "id, " +
            "sum(amount) AS amount, " +
            "datetime " +
            "FROM water WHERE datetime BETWEEN :from AND :to")
    abstract Water getWaterDaySum(long from, long to);

    @Transaction
    public List<Water> getWaterSummary(long from, long to) {
        List<Water> result = new ArrayList<>();

        Calendar since = Calendar.getInstance();
        since.setTimeInMillis(from);
        Calendar before = Calendar.getInstance();
        before.setTimeInMillis(from);

        while (since.getTimeInMillis() < to) {
            before.set(Calendar.HOUR_OF_DAY, 23);
            before.set(Calendar.MINUTE, 59);
            before.set(Calendar.SECOND, before.getActualMaximum(Calendar.SECOND));
            result.add(getWaterDaySum(since.getTimeInMillis(), before.getTimeInMillis()));
            since.add(Calendar.DAY_OF_YEAR, 1);
            before.add(Calendar.DAY_OF_YEAR, 1);
        }

        return result;
    }

}
