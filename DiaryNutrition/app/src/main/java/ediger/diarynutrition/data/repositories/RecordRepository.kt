package ediger.diarynutrition.data.repositories

import ediger.diarynutrition.data.source.dao.RecordDao
import ediger.diarynutrition.data.source.entities.MealAndRecords
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.util.getDayBeginning
import ediger.diarynutrition.util.getDayEnding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import java.util.*

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun getRecords(day: Calendar): List<MealAndRecords> = withContext(Dispatchers.IO) {
        recordDao.getMealsAndRecords(
                day.getDayBeginning().timeInMillis,
                day.getDayEnding().timeInMillis
        )
    }

    suspend fun addRecords(records: List<Record>) = recordDao.populateRecords(records)

    suspend fun addRecord(record: Record) = recordDao.insertRecord(record)

    suspend fun deleteRecord(id: Int) = recordDao.delRecordById(id)

    companion object {
        @Volatile private var instance: RecordRepository? = null

        fun getInstance(recordDao: RecordDao) =
                instance ?: synchronized(this) {
                    instance ?: RecordRepository(recordDao).also { instance = it }
                }
    }

}