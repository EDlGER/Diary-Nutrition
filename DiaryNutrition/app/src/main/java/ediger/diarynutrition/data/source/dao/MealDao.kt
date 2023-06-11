package ediger.diarynutrition.data.source.dao

import androidx.room.*
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal ORDER BY id ASC")
    suspend fun getMeals(): List<Meal>

    @Query("SELECT * FROM meal ORDER BY id ASC")
    fun getMealsFlow(): Flow<List<Meal>>

    @Query("SELECT * FROM meal WHERE id = :id")
    suspend fun getMeal(id: Int): Meal

    @Query("SELECT * FROM meal WHERE user = 1")
    suspend fun getUserMeals(): List<Meal>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMeal(meal: Meal)

    @Query("DELETE FROM meal WHERE id = :id")
    suspend fun deleteMealById(id: Int): Int

    @Query("DELETE FROM meal WHERE user > 0")
    suspend fun deleteUserMeals()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeal(meal: Meal)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun populateMeals(meals: List<Meal>): List<Long>
}