package ediger.diarynutrition.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import ediger.diarynutrition.BACKUP_NAME
import ediger.diarynutrition.DEFAULT_MEALS_COUNT
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.model.JsonDatabaseBackup
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.lang.Exception

class RestoreDatabaseWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    // Showing the notification about the worker progress will be useful for a user

    override suspend fun doWork(): Result = coroutineScope {
        // Need to use Hilt dependency injection
        val database = DiaryDatabase.getInstance(applicationContext)
        try {
            val backupFile = File(applicationContext.filesDir, BACKUP_NAME)
            if (backupFile.exists() && backupFile.length() > 0) {
                applicationContext.openFileInput(BACKUP_NAME).use {
                    val gson = GsonBuilder().create()
                    val backupModel = gson.fromJson<JsonDatabaseBackup>(it.reader(), JsonDatabaseBackup::class.java)

                    val oldFoodIds = backupModel.foodList.map { food -> food.id }
                    backupModel.foodList.toMutableList().forEach { food -> food.id = 0 }

                    val foodIds = database.foodDao().populateFood(backupModel.foodList)
                            .toMutableList()
                            .mapIndexed { index, id ->
                                if (id == -1L) {
                                    return@mapIndexed database.foodDao().getDuplicateId(
                                        backupModel.foodList[index].name,
                                        backupModel.foodList[index].cal
                                    ).toLong()
                                } else id
                            }
                    backupModel.foodList.forEachIndexed { i, food ->
                        if (food.favorite == 1) {
                            database.foodDao().updateFavoriteFoodById(foodIds[i].toInt(), true)
                        }
                    }

                    database.recordDao().deleteAllRecords()
                    database.mealDao().deleteUserMeals()

                    val mealIds = database.mealDao().populateMeals(backupModel.mealList)

                    backupModel.recordList.forEach { record ->
                        if (oldFoodIds.contains(record.foodId)) {
                            record.foodId = foodIds[oldFoodIds.indexOf(record.foodId)].toInt()
                        }
                        if (record.mealId > DEFAULT_MEALS_COUNT) {
                            record.mealId = mealIds[record.mealId - DEFAULT_MEALS_COUNT - 1].toInt()
                        }
                    }
                    database.recordDao().populateRecords(backupModel.recordList)
                    database.weightDao().populateWeight(backupModel.weightList)
                    database.waterDao().populateWater(backupModel.waterList)
                }
            }
        } catch (e: Exception) {
            Result.failure()
        }
        Result.success()
    }

}