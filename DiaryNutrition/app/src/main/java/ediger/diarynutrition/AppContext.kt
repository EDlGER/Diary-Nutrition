package ediger.diarynutrition

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.multidex.MultiDexApplication
import androidx.work.*
import ediger.diarynutrition.billing.BillingClientLifecycle
import ediger.diarynutrition.data.repositories.*
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.DiaryDatabase.Companion.getInstance
import ediger.diarynutrition.database.DbDiary
import ediger.diarynutrition.workers.RemoteDatabaseVersionWorker
import java.util.concurrent.TimeUnit

class AppContext : MultiDexApplication() {

    val database: DiaryDatabase
        get() = getInstance(this)

    val foodRepository: FoodRepository
        get() = FoodRepository.getInstance(database.foodDao())

    val mealRepository: MealRepository
        get() = MealRepository.getInstance(database.mealDao())

    val recordRepository: RecordRepository
        get() = RecordRepository.getInstance(database.recordDao())

    val summaryRepository: SummaryRepository
        get() = SummaryRepository.getInstance(database.summaryDao())

    val waterRepository: WaterRepository
        get() = WaterRepository.getInstance(database.waterDao())

    val weightRepository: WeightRepository
        get() = WeightRepository.getInstance(database.weightDao())

    val billingClientLifecycle: BillingClientLifecycle
        get() = BillingClientLifecycle.getInstance(this)

    private val Context.dataStore by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )

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
    }

    companion object {
        //TODO delete
        //Date of chosen day (begin of the day - 00:00) in milliseconds
        // var date: Long = 0
        val dbDiary: DbDiary? = null

    }
}