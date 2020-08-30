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

    @Query("SELECT DISTINCT id FROM food WHERE name = :name AND cal = :cal")
    suspend fun getDuplicateId(name: String, cal: Float): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun populateFood(foodList: List<Food>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllFood(foodList: List<Food>): List<Long>

    @Update
    fun updateFood(food: Food)

    @Query("UPDATE food " +
            "SET verified = :verified, " +
            "gi = :gi, " +
            "user = :user," +
            "favorite = (SELECT favorite FROM food WHERE name = :name AND cal = :cal) " +
            "WHERE id IN (SELECT id FROM food WHERE name = :name AND cal = :cal)")
    fun updateFoodByNameAndCal(name: String, cal: Float, verified: Int, gi: Int, user: Int)

    @Query("UPDATE food SET favorite = 1 WHERE id = :id")
    suspend fun updateFavoriteFoodById(id: Int)

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

    //Returns user, favorite, and food that is being used in record table
    @Query("SELECT * FROM food " +
            "WHERE favorite = 1 OR user = 1 " +
            "OR id IN (SELECT food_id FROM record)")
    suspend fun getBackupFood(): List<Food>

    //FTS4?
}