package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food ORDER BY name ASC")
    fun getAllFood(): LiveData<List<Food>>

    @Query("SELECT * FROM food WHERE favorite = 1 AND user != 2 ORDER BY name ASC")
    fun getFavorFood(): LiveData<List<Food>>

    @Query("SELECT * FROM food WHERE user = 1 ORDER BY name ASC")
    fun getUserFood(): LiveData<List<Food>>

    @Query("SELECT * FROM food WHERE id = :id")
    fun getFood(id: Int): LiveData<Food>

    @Query("SELECT food.* FROM food INNER JOIN record ON record.food_id = food.id " +
            "WHERE (datetime between :from AND :to) AND food.user != 2 " +
            "GROUP BY food.id " +
            "ORDER BY count(record.datetime) DESC")
    fun getPopularFood(from: Long, to: Long): PagingSource<Int, Food>

    @Query("SELECT DISTINCT id FROM food WHERE name = :name AND cal = :cal")
    suspend fun getDuplicateId(name: String, cal: Float): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun populateFood(foodList: List<Food>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllFood(foodList: List<Food>): List<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateFood(food: Food)

    @Query("UPDATE food " +
            "SET verified = :verified, " +
            "gi = :gi, " +
            "user = :user," +
            "favorite = (SELECT favorite FROM food WHERE name = :name AND cal = :cal) " +
            "WHERE id IN (SELECT id FROM food WHERE name = :name AND cal = :cal)")
    fun updateFoodByNameAndCal(name: String, cal: Float, verified: Int, gi: Int, user: Int)

    @Query("UPDATE food SET favorite = CASE WHEN :favorite THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun updateFavoriteFoodById(id: Int, favorite: Boolean)

    @Query("UPDATE food SET favorite = :favorite WHERE id = :id AND favorite != :favorite")
    suspend fun updateFavoriteFoodIfDiff(id: Int, favorite: Int)

    @Query("UPDATE food SET user = :user WHERE id = :id AND user != :user")
    suspend fun updateUserFoodIfDiff(id: Int, user: Int)

    @Query("UPDATE food SET user = 2 WHERE id = :id")
    suspend fun hideUserFood(id: Int)

    @Query("DELETE FROM food WHERE id = :id")
    suspend fun deleteFoodById(id: Int): Int

    @Query("SELECT * FROM food WHERE name LIKE '%' || :text || '%' AND user != 2 " +
            "ORDER BY (name LIKE :text || '%') DESC, verified DESC, name ASC")
    fun searchAllFood(text: String): PagingSource<Int, Food>

    @Query("SELECT * FROM food WHERE favorite = 1 AND name LIKE '%' || :text || '%' AND user != 2 " +
            "ORDER BY (name LIKE :text || '%') DESC, name ASC")
    fun searchFavoriteFood(text: String): PagingSource<Int, Food>

    @Query("SELECT * FROM food WHERE user = 1 AND name LIKE '%' || :text || '%' ORDER BY (name LIKE :text || '%') DESC, name ASC")
    fun searchUserFood(text: String): PagingSource<Int, Food>

    //Returns user, favorite, and food that is being used in record table
    @Query("SELECT * FROM food " +
            "WHERE favorite = 1 OR user > 0 " +
            "OR id IN (SELECT food_id FROM record)")
    suspend fun getBackupFood(): List<Food>

    @Query("DELETE FROM food")
    suspend fun deleteAllFood()

}