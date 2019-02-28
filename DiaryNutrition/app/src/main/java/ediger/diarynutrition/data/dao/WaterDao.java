package ediger.diarynutrition.data.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import ediger.diarynutrition.data.Water;

@Dao
public interface WaterDao {

    /**
     * Get water records for specific period
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT * FROM water WHERE datetime BETWEEN :from AND :to ORDER BY datetime ASC")
    LiveData<List<Water>> loadWater(long from, long to);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertWater(int amount, long datetime);

    @Query("DELETE FROM water WHERE _id = :id")
    int deleteWaterById(int id);

}
