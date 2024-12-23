package ediger.diarynutrition.data.repositories

import ediger.diarynutrition.data.source.dao.MealDao
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.flow.Flow

class MealRepository(private val mealDao: MealDao) {

    val mealsFlow: Flow<List<Meal>>
        get() = mealDao.getMealsFlow()

    suspend fun getMeals(): List<Meal> = mealDao.getMeals()

    suspend fun getMeal(id: Int) = mealDao.getMeal(id)

    suspend fun insertMeal(meal: Meal) {
        mealDao.insertMeal(meal)
    }

    suspend fun deleteMeal(id: Int) {
        mealDao.deleteMealById(id)
    }

    suspend fun updateMeal(meal: Meal) {
        mealDao.updateMeal(meal)
    }

    companion object {
        @Volatile private var instance: MealRepository? = null

        fun getInstance(mealDao: MealDao) =
                instance ?: synchronized(this) {
                    instance ?: MealRepository(mealDao).also { instance = it }
                }
    }

}