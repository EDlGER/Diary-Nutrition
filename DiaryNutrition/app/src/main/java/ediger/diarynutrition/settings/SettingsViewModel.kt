package ediger.diarynutrition.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import ediger.diarynutrition.ERROR_RESTORE_FILE_MISSING
import ediger.diarynutrition.Event
import ediger.diarynutrition.R
import ediger.diarynutrition.workers.BackupDatabaseWorker
import ediger.diarynutrition.workers.RestoreDatabaseWorker

class SettingsViewModel(app: Application): AndroidViewModel(app) {

    private val workManager = WorkManager.getInstance(app)

    val backupStatus: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(BackupDatabaseWorker.NAME)

    val restoreStatus: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(RestoreDatabaseWorker.NAME)

    private var isBackupRequested = false
    private var isRestoreRequested = false

    private val _snackbarMessage = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarMessage

    fun backupDatabase() {
        val request = OneTimeWorkRequest.from(BackupDatabaseWorker::class.java)
        workManager.enqueueUniqueWork(
            BackupDatabaseWorker.NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        isBackupRequested = true
    }

    fun restoreDatabase() {
        val request = OneTimeWorkRequest.from(RestoreDatabaseWorker::class.java)
        workManager.enqueueUniqueWork(
            RestoreDatabaseWorker.NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        isRestoreRequested = true
    }

    fun concludeBackup(listOfInfos: List<WorkInfo>) {
        if (listOfInfos.isNotEmpty() && isBackupRequested) {
            val message: Int? = when (listOfInfos[0].state) {
                WorkInfo.State.SUCCEEDED -> R.string.message_data_backup
                WorkInfo.State.FAILED -> R.string.message_data_backup_fail
                else -> { null }
            }
            message?.let {
                _snackbarMessage.value = Event(it)
                isBackupRequested = false
            }
        }
    }

    fun concludeRestore(listOfInfos: List<WorkInfo>) {
        if (listOfInfos.isNotEmpty() && isRestoreRequested) {
            val workInfo = listOfInfos[0]
            val message: Int? = when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> R.string.message_data_restore
                WorkInfo.State.FAILED -> {
                    val isFileMissing = workInfo.outputData
                        .getBoolean(ERROR_RESTORE_FILE_MISSING, false)
                    if (isFileMissing) {
                        R.string.message_data_restore_file_missing
                    } else {
                        R.string.message_data_restore_fail
                    }
                }
                else -> { null }
            }
            message?.let {
                _snackbarMessage.value = Event(it)
                isRestoreRequested = false
            }

        }
    }

}