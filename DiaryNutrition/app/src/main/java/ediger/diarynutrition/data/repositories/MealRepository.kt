package ediger.diarynutrition.data.repositories

import ediger.diarynutrition.data.source.dao.MealDao
import ediger.diarynutrition.data.source.entities.Meal

class MealRepository(private val mealDao: MealDao) {

    suspend fun getMeals(): List<Meal> = mealDao.getMeals()

    companion object {
        @Volatile private var instance: MealRepository? = null

        fun getInstance(mealDao: MealDao) =
                instance ?: synchronized(this) {
                    instance ?: MealRepository(mealDao).also { instance = it }
                }
    }

}