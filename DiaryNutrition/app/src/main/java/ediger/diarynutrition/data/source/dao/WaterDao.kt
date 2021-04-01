package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ediger.diarynutrition.data.source.entities.Water
import java.util.*

@Dao
interface WaterDao {
    /**
     * Get water records for specific period
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT * FROM water WHERE datetime BETWEEN :from AND :to ORDER BY datetime ASC")
    fun getWaterList(from: Long, to: Long): LiveData<List<Water>>

    @Query("SELECT * FROM water")
    suspend fun getWater(): List<Water>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWater(water: Water)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun populateWater(water: List<Water>)

    @Query("DELETE FROM water WHERE id = :id")
    suspend fun deleteWaterById(id: Int): Int

    @Query("SELECT " +
            "id, " +
            "sum(amount) AS amount, " +
            "datetime " +
            "FROM water WHERE datetime BETWEEN :from AND :to")
    fun getWaterSum(from: Long, to: Long): LiveData<Water>

}