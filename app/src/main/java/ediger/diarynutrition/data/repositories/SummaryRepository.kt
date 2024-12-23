package ediger.diarynutrition.data.repositories

import androidx.lifecycle.LiveData
import ediger.diarynutrition.data.source.dao.SummaryDao
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.data.source.entities.Water
import ediger.diarynutrition.util.getDayBeginning
import ediger.diarynutrition.util.getDayEnding
import java.util.*

class SummaryRepository(private val summaryDao: SummaryDao) {

    fun getDaySummary(day: Calendar): LiveData<Summary> {
        val from = day.getDayBeginning().timeInMillis
        val to = day.getDayEnding().timeInMillis
        return summaryDao.getDaySummary(from, to)
    }

    suspend fun getSummary(since: Calendar, numberOfDays: Int): List<Summary> {
        val before = (since.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, numberOfDays - 1)
        }
        val from = since.getDayBeginning().timeInMillis
        val to = before.getDayEnding().timeInMillis
        return summaryDao.getMacroSummary(from, to)
    }

    suspend fun getWaterSummary(since: Calendar, numberOfDays: Int): List<Water> {
        val before = (since.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, numberOfDays - 1)
        }
        val from = since.getDayBeginning().timeInMillis
        val to = before.getDayEnding().timeInMillis
        return summaryDao.getWaterSummary(from, to)
    }

    companion object {
        @Volatile private var instance: SummaryRepository? = null

        fun getInstance(summaryDao: SummaryDao) =
                instance ?: synchronized(this) {
                    instance ?: SummaryRepository(summaryDao).also { instance = it }
                }
    }
}