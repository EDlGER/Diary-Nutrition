package ediger.diarynutrition.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ediger.diarynutrition.data.source.dao.FoodDao
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    fun searchAllFood(query: String): Flow<PagingData<Food>> {
        val newQuery = query.replace(" ", "%")
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.searchAllFood("%$newQuery%")
        }.flow
    }

    fun searchFavoriteFood(query: String): Flow<PagingData<Food>> {
        val newQuery = query.replace(" ", "%")
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.searchFavoriteFood("%$newQuery%")
        }.flow
    }

    fun searchUserFood(query: String): Flow<PagingData<Food>> {
        val newQuery = query.replace(" ", "%")
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.searchUserFood("%$newQuery%")
        }.flow
    }

    suspend fun setFavorite(id: Int, favorite: Boolean) {
        foodDao.updateFavoriteFoodById(id, favorite)
    }

    suspend fun deleteFood(id: Int) = foodDao.deleteFoodById(id)

    suspend fun addFood(food: Food) = foodDao.insertFood(food)

    suspend fun updateFood(food: Food) = foodDao.updateFood(food)

    fun getFood(id: Int) = foodDao.getFood(id)

    companion object {

        @Volatile private var instance: FoodRepository? = null
        private const val PAGE_SIZE = 40

        fun getInstance(foodDao: FoodDao) =
            instance ?: synchronized(this) {
                instance ?: FoodRepository(foodDao).also { instance = it }
            }

    }

}
