package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.data.source.entities.Water
import java.util.*

@Dao
abstract class SummaryDao {
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
    abstract fun getDaySummary(from: Long, to: Long): LiveData<Summary>

    @Query("SELECT " +
            "sum(food.cal / 100 * record.serving) as cal, " +
            "sum(food.prot / 100 * record.serving) as prot, " +
            "sum(food.fat / 100 * record.serving) as fat, " +
            "sum(food.carbo / 100 * record.serving) as carbo " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food.id " +
            "WHERE datetime BETWEEN :from AND :to")
    abstract suspend fun getSummary(from: Long, to: Long): Summary

    @Transaction
    open suspend fun getMacroSummary(from: Long, to: Long): List<Summary> {
        val result: MutableList<Summary> = ArrayList()
        val since = Calendar.getInstance().apply { timeInMillis = from }
        val before = Calendar.getInstance().apply { timeInMillis = from }

        while (since.timeInMillis < to) {
            before[Calendar.HOUR_OF_DAY] = 23
            before[Calendar.MINUTE] = 59
            before[Calendar.SECOND] = before.getActualMaximum(Calendar.SECOND)
            result.add(
                    element = getSummary(since.timeInMillis, before.timeInMillis)
            )
            since.add(Calendar.DAY_OF_YEAR, 1)
            before.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    @Query("SELECT " +
            "id, " +
            "sum(amount) AS amount, " +
            "datetime " +
            "FROM water WHERE datetime BETWEEN :from AND :to")
    abstract fun getWaterDaySum(from: Long, to: Long): Water

    @Transaction
    open suspend fun getWaterSummary(from: Long, to: Long): List<Water> {
        val result: MutableList<Water> = ArrayList()
        val since = Calendar.getInstance().apply { timeInMillis = from }
        val before = Calendar.getInstance().apply { timeInMillis = from }

        while (since.timeInMillis < to) {
            before[Calendar.HOUR_OF_DAY] = 23
            before[Calendar.MINUTE] = 59
            before[Calendar.SECOND] = before.getActualMaximum(Calendar.SECOND)
            result.add(
                    element = getWaterDaySum(since.timeInMillis, before.timeInMillis)
            )
            since.add(Calendar.DAY_OF_YEAR, 1)
            before.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }
}