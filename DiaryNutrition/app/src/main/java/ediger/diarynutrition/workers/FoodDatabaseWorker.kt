package ediger.diarynutrition.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.opencsv.bean.*
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.coroutineScope
import java.io.*
import java.lang.Exception

class FoodDatabaseWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val language = PreferenceHelper.getValue(PreferenceHelper.KEY_LANGUAGE_DB, String::class.java, "en")
            applicationContext.assets.open("databases/food_$language.csv").use {
                val columns = arrayOf("id", "name", "cal", "carbo", "prot", "fat", "verified", "gi")
                val strat = ColumnPositionMappingStrategy<Food>().apply {
                    type = Food::class.java
                    setColumnMapping(*columns)
                }
                val foodList = CsvToBeanBuilder<Food>(BufferedReader(it.reader()))
                        .withMappingStrategy(strat)
                        .build()
                        .parse()

                val database = DiaryDatabase.getInstance(applicationContext)
                database.foodDao().insertAllFood(foodList)
            }
            Result.success()
        } catch (ex: Exception) {
            //throw ex
            Result.failure()
        }
    }
}

