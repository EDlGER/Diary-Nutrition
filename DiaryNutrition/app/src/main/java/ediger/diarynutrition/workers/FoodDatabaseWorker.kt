package ediger.diarynutrition.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.opencsv.bean.*
import ediger.diarynutrition.DATABASE_VERSION
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.KEY_LOCAL_DB_VERSION
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Exception

class FoodDatabaseWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val language = inputData.getString(KEY_LANGUAGE_DB) ?: return@withContext Result.failure()
            applicationContext.assets.open("databases/food_$language.csv").use {
                val columns = arrayOf("name", "cal", "carbo", "prot", "fat", "verified", "gi")
                val strat = ColumnPositionMappingStrategy<Food>().apply {
                    type = Food::class.java
                    setColumnMapping(*columns)
                }
                val foodList = CsvToBeanBuilder<Food>(BufferedReader(it.reader()))
                        .withMappingStrategy(strat)
                        .build()
                        .parse()

                val database = DiaryDatabase.getInstance(applicationContext)
                database.foodDao().populateFood(foodList)

                PreferenceHelper.setValue(KEY_LOCAL_DB_VERSION, DATABASE_VERSION)
                PreferenceHelper.setValue(KEY_LANGUAGE_DB, language)

                Result.success()
            }
        } catch (ex: Exception) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                RestoreDatabaseWorker.NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.from(RestoreDatabaseWorker::class.java)
            )
            Result.failure()
        }
    }
}

