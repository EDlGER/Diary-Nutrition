package ediger.diarynutrition.data.repositories

import androidx.lifecycle.LiveData
import ediger.diarynutrition.data.source.dao.WeightDao
import ediger.diarynutrition.data.source.entities.Weight
import ediger.diarynutrition.util.getDayBeginning
import ediger.diarynutrition.util.getDayEnding
import java.util.*

class WeightRepository(private val weightDao: WeightDao) {

    fun getWeight(): LiveData<List<Weight>> = weightDao.loadWeight()

    fun getWeight(since: Calendar): LiveData<List<Weight>> {
        val from = since.getDayBeginning().timeInMillis
        val to = Calendar.getInstance().getDayEnding().timeInMillis
        return weightDao.getWeight(from, to)
    }

    suspend fun getWeightForDay(day: Calendar): Weight? {
        val from = day.getDayBeginning().timeInMillis
        val to = day.getDayEnding().timeInMillis
        return weightDao.getWeightForDay(from, to)
    }

    suspend fun addWeight(weight: Weight) = weightDao.insertWeight(weight)

    suspend fun updateWeight(weight: Weight) = weightDao.updateWeight(weight)

    suspend fun deleteWeight(id: Int) = weightDao.deleteWeightById(id)

    companion object {
        @Volatile private var instance: WeightRepository? = null

        fun getInstance(weightDao: WeightDao) =
                instance ?: synchronized(this) {
                    instance ?: WeightRepository(weightDao).also { instance = it }
                }
    }

}