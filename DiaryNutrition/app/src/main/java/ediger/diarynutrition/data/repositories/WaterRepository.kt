package ediger.diarynutrition.data.repositories

import androidx.lifecycle.LiveData
import ediger.diarynutrition.data.source.dao.WaterDao
import ediger.diarynutrition.data.source.entities.Water
import ediger.diarynutrition.util.getDayBeginning
import ediger.diarynutrition.util.getDayEnding
import java.util.*

class WaterRepository(private val waterDao: WaterDao) {

    fun getWaterList(day: Calendar): LiveData<List<Water>> {
        val from = day.getDayBeginning().timeInMillis
        val to = day.getDayEnding().timeInMillis
        return waterDao.getWaterList(from, to)
    }

    fun getWaterSum(day: Calendar): LiveData<Water> {
        val from = day.getDayBeginning().timeInMillis
        val to = day.getDayEnding().timeInMillis
        return waterDao.getWaterSum(from, to)
    }

    suspend fun addWater(water: Water) = waterDao.insertWater(water)

    suspend fun deleteWater(id: Int) = waterDao.deleteWaterById(id)

    companion object {
        @Volatile private var instance: WaterRepository? = null

        fun getInstance(waterDao: WaterDao) =
                instance ?: synchronized(this) {
                    instance ?: WaterRepository(waterDao).also { instance = it }
                }
    }

}