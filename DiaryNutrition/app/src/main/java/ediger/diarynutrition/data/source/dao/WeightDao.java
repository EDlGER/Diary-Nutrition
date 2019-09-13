package ediger.diarynutrition.data.source.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import ediger.diarynutrition.data.source.entities.Weight;

@Dao
public interface WeightDao {

    @Query("SELECT * FROM weight ORDER BY datetime DESC")
    LiveData<List<Weight>> loadWeight();

    /**
     * Get all records of weight for specific interval
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT * FROM weight WHERE datetime BETWEEN :from AND :to ORDER BY datetime ASC")
    LiveData<List<Weight>> getWeight(long from, long to);

    @Query("SELECT * FROM weight ORDER BY datetime DESC LIMIT 1")
    Weight getLastWeight();

    @Query("SELECT * FROM weight WHERE datetime BETWEEN :from AND :to")
    Weight getWeightForDay(long from, long to);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeight(Weight weight);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateWeight(Weight weight);

    @Query("DELETE FROM weight WHERE id = :id")
    int deleteWeightById(int id);

}
