package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ediger.diarynutrition.data.source.entities.Weight

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight ORDER BY datetime DESC")
    fun loadWeight(): LiveData<List<Weight>>

    /**
     * Get all records of weight for specific interval
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT * FROM weight WHERE datetime BETWEEN :from AND :to ORDER BY datetime ASC")
    fun getWeight(from: Long, to: Long): LiveData<List<Weight>>

    @Query("SELECT * FROM weight")
    suspend fun getWeight(): List<Weight>

    @Query("SELECT * FROM weight ORDER BY datetime DESC LIMIT 1")
    fun lastWeight(): Weight

    @Query("SELECT * FROM weight WHERE datetime BETWEEN :from AND :to")
    suspend fun getWeightForDay(from: Long, to: Long): Weight?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: Weight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun populateWeight(weights: List<Weight>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWeight(weight: Weight)

    @Query("DELETE FROM weight WHERE id = :id")
    suspend fun deleteWeightById(id: Int): Int
}