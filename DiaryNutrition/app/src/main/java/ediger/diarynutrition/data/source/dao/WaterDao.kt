package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ediger.diarynutrition.data.source.entities.Water
import java.util.*

@Dao
abstract class WaterDao {
    /**
     * Get water records for specific period
     * @param from beginning of the period in milliseconds
     * @param to end of the period in milliseconds
     */
    @Query("SELECT * FROM water WHERE datetime BETWEEN :from AND :to ORDER BY datetime ASC")
    abstract fun getWaterList(from: Long, to: Long): LiveData<List<Water>>

    @Query("SELECT * FROM water")
    abstract suspend fun getWater(): List<Water>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertWater(water: Water?)

    @Query("DELETE FROM water WHERE id = :id")
    abstract fun deleteWaterById(id: Int): Int

    @Query("SELECT " +
            "id, " +
            "sum(amount) AS amount, " +
            "datetime " +
            "FROM water WHERE datetime BETWEEN :from AND :to")
    abstract fun getWaterSum(from: Long, to: Long): LiveData<Water>

    @Query("SELECT " +
            "id, " +
            "sum(amount) AS amount, " +
            "datetime " +
            "FROM water WHERE datetime BETWEEN :from AND :to")
    abstract fun getWaterDaySum(from: Long, to: Long): Water

    @Transaction
    open fun getWaterSummary(from: Long, to: Long): List<Water>? {
        val result: MutableList<Water> = ArrayList()
        val since = Calendar.getInstance()
        since.timeInMillis = from
        val before = Calendar.getInstance()
        before.timeInMillis = from
        while (since.timeInMillis < to) {
            before[Calendar.HOUR_OF_DAY] = 23
            before[Calendar.MINUTE] = 59
            before[Calendar.SECOND] = before.getActualMaximum(Calendar.SECOND)
            result.add(getWaterDaySum(since.timeInMillis, before.timeInMillis))
            since.add(Calendar.DAY_OF_YEAR, 1)
            before.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }
}