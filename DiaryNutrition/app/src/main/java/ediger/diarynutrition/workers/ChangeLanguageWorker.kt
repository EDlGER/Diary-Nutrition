package ediger.diarynutrition.workers

import android.content.Context
import android.content.res.Configuration
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class ChangeLanguagePreparationsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        val language = inputData.getString(KEY_LANGUAGE_DB) ?: return@withContext Result.failure()
        val database = (applicationContext as AppContext).database
        try {
            database.recordDao().deleteAllRecords()
            database.foodDao().deleteAllFood()
            database.mealDao().deleteUserMeals()

            val config = Configuration(applicationContext.resources.configuration).apply {
                setLocale(Locale(language))
            }
            val mealNames = applicationContext.createConfigurationContext(config).resources
                .getStringArray(R.array.meals)
            val updatedMeals = database.mealDao().getMeals()
                .onEachIndexed { i, meal -> meal.name = mealNames[i] }

            for (meal in updatedMeals) {
                database.mealDao().updateMeal(meal)
            }

            Result.success()
        } catch (e: Exception) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                RestoreDatabaseWorker.NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.from(RestoreDatabaseWorker::class.java)
            )
            Result.failure()
        }
    }

}