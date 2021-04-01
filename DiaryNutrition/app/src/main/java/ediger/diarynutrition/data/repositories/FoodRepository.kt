package ediger.diarynutrition.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ediger.diarynutrition.MILLIS_WEEK
import ediger.diarynutrition.data.source.dao.FoodDao
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    fun searchAllFood(query: String): Flow<PagingData<Food>> {
        val newQuery = query.trim().replace(" ", "%")
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.searchAllFood(newQuery)
        }.flow
    }

    fun searchFavoriteFood(query: String): Flow<PagingData<Food>> {
        val newQuery = query.trim().replace(" ", "%")
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.searchFavoriteFood(newQuery)
        }.flow
    }

    fun searchUserFood(query: String): Flow<PagingData<Food>> {
        val newQuery = query.trim().replace(" ", "%")
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.searchUserFood(newQuery)
        }.flow
    }

    suspend fun setFavorite(id: Int, favorite: Boolean) {
        foodDao.updateFavoriteFoodById(id, favorite)
    }

    suspend fun deleteFood(id: Int) = foodDao.hideUserFood(id)

    suspend fun addFood(food: Food) = foodDao.insertFood(food)

    suspend fun updateFood(food: Food) = foodDao.updateFood(food)

    fun getFood(id: Int) = foodDao.getFood(id)

    fun getPopularFood(): Flow<PagingData<Food>> {
        val to = System.currentTimeMillis()
        val from = to - MILLIS_WEEK
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            foodDao.getPopularFood(from, to)
        }.flow
    }

    companion object {

        @Volatile private var instance: FoodRepository? = null
        private const val PAGE_SIZE = 40

        fun getInstance(foodDao: FoodDao) =
            instance ?: synchronized(this) {
                instance ?: FoodRepository(foodDao).also { instance = it }
            }

    }

}
