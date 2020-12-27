package ediger.diarynutrition

import androidx.multidex.MultiDexApplication
import androidx.work.*
import ediger.diarynutrition.data.DiaryRepository
import ediger.diarynutrition.data.FoodRepository
import ediger.diarynutrition.data.MealRepository
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.DiaryDatabase.Companion.getInstance
import ediger.diarynutrition.database.DbDiary
import ediger.diarynutrition.workers.RemoteDatabaseVersionWorker
import java.util.concurrent.TimeUnit

class AppContext : MultiDexApplication() {
    private val mAppExecutors = AppExecutors()

    val database: DiaryDatabase
        get() = getInstance(this)

    val repository: DiaryRepository
        get() = DiaryRepository.getInstance(database, mAppExecutors)

    val foodRepository: FoodRepository
        get() = FoodRepository.getInstance(database.foodDao())

    val mealRepository: MealRepository
        get() = MealRepository.getInstance(database.mealDao())

    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(applicationContext)
        if (!BuildConfig.DEBUG) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            val work = PeriodicWorkRequestBuilder<RemoteDatabaseVersionWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    RemoteDatabaseVersionWorker.TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
            )
        }
        //TODO delete
        //sDbDiary = new DbDiary(this);
    }

    companion object {
        //TODO delete
        //Date of chosen day (begin of the day - 00:00) in milliseconds
        // var date: Long = 0
        val dbDiary: DbDiary? = null
        lateinit var instance: AppContext

    }
}