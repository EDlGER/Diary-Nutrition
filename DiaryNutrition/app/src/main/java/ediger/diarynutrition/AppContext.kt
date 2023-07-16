package ediger.diarynutrition

import androidx.multidex.MultiDexApplication
import androidx.work.*
import ediger.diarynutrition.billing.BillingClientLifecycle
import ediger.diarynutrition.data.repositories.*
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.DiaryDatabase.Companion.getInstance
import ediger.diarynutrition.workers.BackupDatabaseWorker
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

            val backupWork = PeriodicWorkRequestBuilder<BackupDatabaseWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
                .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                BackupDatabaseWorker.NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                backupWork
            )
        }
    }

}