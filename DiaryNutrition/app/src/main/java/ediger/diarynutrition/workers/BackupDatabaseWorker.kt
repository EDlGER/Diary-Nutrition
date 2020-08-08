package ediger.diarynutrition.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.data.source.model.JsonDatabaseBackup
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

class BackupDatabaseWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = (applicationContext as AppContext).database

            val jsonBackup = JsonDatabaseBackup(
                    recordList = database.recordDao().getRecords(),
                    foodList = database.foodDao().getBackupFood(),
                    mealList = database.mealDao().getUserMeals(),
                    waterList = database.waterDao().getWater(),
                    weightList = database.weightDao().getWeight()
            )
            val gson = GsonBuilder().setPrettyPrinting().create()

            applicationContext.openFileOutput("backupFile.json", Context.MODE_PRIVATE).use {
                it.write(gson.toJson(jsonBackup).toByteArray())
            }

        } catch (e: Exception) {
            Result.failure()
        }

        Result.success()
    }

    companion object {
        const val TAG = "BackupDatabaseWorker"
    }
}