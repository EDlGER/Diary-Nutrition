package ediger.diarynutrition.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ediger.diarynutrition.data.source.entities.Food

@Dao
interface FoodDao {
    @Query("SELECT * FROM food ORDER BY name ASC")
    fun getAllFood(): LiveData<List<Food>>

    @Query("SELECT * FROM food WHERE favorite = 1 ORDER BY name ASC")
    fun getFavorFood(): LiveData<List<Food>>

    @Query("SELECT * FROM food WHERE user = 1 ORDER BY name ASC")
    fun getUserFood(): LiveData<List<Food>>

    @Query("SELECT * FROM food WHERE id = :id")
    fun getFood(id: Int): Food

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllFood(foodList: List<Food>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun populateAllFood(foodList: List<Food>)

    @Update
    fun updateFood(food: Food)

    @Query("DELETE FROM food WHERE id = :id")
    fun deleteFoodById(id: Int): Int

    @Query("SELECT * FROM food " +
            "WHERE name LIKE :text " +
            "ORDER BY name = :text DESC, name LIKE :text DESC ")
    fun searchAllFood(text: String?): LiveData<List<Food>>

    @Query("SELECT * FROM food " +
            "WHERE favorite = 1 AND name LIKE :text " +
            "ORDER BY name = :text DESC, name LIKE :text DESC ")
    fun searchFavorFood(text: String?): LiveData<List<Food>>

    @Query("SELECT * FROM food " +
            "WHERE user = 1 AND name LIKE :text " +
            "ORDER BY name = :text DESC, name LIKE :text DESC ")
    fun searchUsrFood(text: String?): LiveData<List<Food>>

    //FTS4?
}