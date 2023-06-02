package ediger.diarynutrition.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import ediger.diarynutrition.workers.BackupDatabaseWorker

class SettingsViewModel(app: Application): AndroidViewModel(app) {

    private val workManager = WorkManager.getInstance(app)

    val backupStatus: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(BackupDatabaseWorker.NAME)

    fun backupDatabase() {
        val request = OneTimeWorkRequest.from(BackupDatabaseWorker::class.java)

        workManager.enqueueUniqueWork(
            BackupDatabaseWorker.NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

}