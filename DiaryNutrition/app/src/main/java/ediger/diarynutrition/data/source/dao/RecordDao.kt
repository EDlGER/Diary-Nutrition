package ediger.diarynutrition.data.source.dao

import androidx.room.*
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.data.source.entities.MealAndRecords
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.data.source.entities.RecordAndFood
import java.util.*

@Dao
abstract class RecordDao {
    @Query("SELECT * FROM meal")
    abstract fun getMeals(): List<Meal>

    @Query("SELECT " +
            "record.*, " +
            "food.name as f_name, " +
            "(food.cal / 100 * record.serving) as f_cal, " +
            "(food.prot / 100 * record.serving) as f_prot, " +
            "(food.fat / 100 * record.serving) as f_fat, " +
            "(food.carbo / 100 * record.serving) as f_carbo " +
            "FROM record " +
            "INNER JOIN food ON record.food_id = food.id " +
            "WHERE meal_id = :mealId AND datetime BETWEEN :from AND :to")
    abstract fun getRecordsAndFoods(mealId: Long, from: Long, to: Long): List<RecordAndFood>

    @Transaction
    open fun getMealsAndRecords(from: Long, to: Long): List<MealAndRecords> {
        //MutableLiveData<List<MealAndRecords>> result = new MutableLiveData<>();
        val mealAndRecordsList: MutableList<MealAndRecords> = ArrayList()
        val meals = getMeals()
        for (meal in meals) {
            val mealAndRecords = MealAndRecords(
                    meal,
                    getRecordsAndFoods(meal.id.toLong(), from, to)
            )
            mealAndRecordsList.add(mealAndRecords)
        }
        //result.postValue(mealAndRecordsList);
        return mealAndRecordsList
    }

    @Query("SELECT * FROM record WHERE id = :id")
    abstract fun getRecord(id: Int): Record

    @Query("SELECT * FROM record")
    abstract suspend fun getRecords(): List<Record>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertRecord(record: Record)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun populateRecords(records: List<Record>)

    @Query("DELETE FROM record WHERE id = :id")
    abstract fun delRecordById(id: Int): Int

    @Query("DELETE FROM record")
    abstract fun deleteAllRecords()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateRecord(record: Record)
}