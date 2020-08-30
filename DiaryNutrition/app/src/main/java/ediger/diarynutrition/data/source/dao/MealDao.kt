package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ediger.diarynutrition.data.source.entities.Meal

@Dao
interface MealDao {
    @Query("SELECT * FROM meal ORDER BY id ASC")
    fun getMeals(): LiveData<List<Meal>>

    @Query("SELECT * FROM meal WHERE id = :id")
    fun getMeal(id: Int): Meal

    @Query("SELECT * FROM meal WHERE user = 1")
    suspend fun getUserMeals(): List<Meal>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMeal(meal: Meal)

    @Query("DELETE FROM meal WHERE id = :id")
    fun deleteMealById(id: Int): Int

    @Query("DELETE FROM meal WHERE user = 1")
    fun deleteUserMeals()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMeal(meal: Meal)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun populateMeals(meals: List<Meal>): List<Long>
}