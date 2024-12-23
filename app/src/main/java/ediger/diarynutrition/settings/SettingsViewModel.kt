package ediger.diarynutrition.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.*
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.ERROR_RESTORE_FILE_MISSING
import ediger.diarynutrition.Event
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.KEY_WEIGHT
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.workers.BackupDatabaseWorker
import ediger.diarynutrition.workers.ChangeLanguagePreparationsWorker
import ediger.diarynutrition.workers.FoodDatabaseWorker
import ediger.diarynutrition.workers.RestoreDatabaseWorker
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application): AndroidViewModel(app) {

    private val workManager = WorkManager.getInstance(app)

    val backupStatus: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(BackupDatabaseWorker.NAME)

    val restoreStatus: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(RestoreDatabaseWorker.NAME)

    var changeLangStatus: LiveData<List<WorkInfo>>? = null

    private val _isBackupRequested = MutableLiveData(false)
    val isBackupRequested: LiveData<Boolean> = _isBackupRequested

    private var _isRestoreRequested = MutableLiveData(false)
    val isRestoreRequested: LiveData<Boolean> = _isRestoreRequested

    private val _isChangeLangRequested = MutableLiveData(false)
    val isChangeLangRequested: LiveData<Boolean> = _isChangeLangRequested

    private val _snackbarMessage = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarMessage

    private val weightRepository = (app as AppContext).weightRepository

    fun backupDatabase() {
        val request = OneTimeWorkRequest.from(BackupDatabaseWorker::class.java)
        workManager.enqueueUniqueWork(
            BackupDatabaseWorker.NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        _isBackupRequested.value = true
    }

    fun restoreDatabase() {
        val request = OneTimeWorkRequest.from(RestoreDatabaseWorker::class.java)
        workManager.enqueueUniqueWork(
            RestoreDatabaseWorker.NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        _isRestoreRequested.value = true
    }

    // In case of the autoBackup feature
    fun restoreIfNecessary() = viewModelScope.launch {
        val sharedPrefWeight =
            PreferenceHelper.getValue(KEY_WEIGHT, Float::class.javaObjectType, 0F)
        val weightList = weightRepository.getAllWeight()
        if (sharedPrefWeight != 0F && weightList.isEmpty()) {
            restoreDatabase()
            _isRestoreRequested.value = false
        }
    }

    fun concludeBackup(listOfInfos: List<WorkInfo>) {
        if (listOfInfos.isNotEmpty() && _isBackupRequested.value!!) {
            val message: Int? = when (listOfInfos[0].state) {
                WorkInfo.State.SUCCEEDED -> R.string.message_data_backup
                WorkInfo.State.FAILED -> R.string.message_data_backup_fail
                else -> { null }
            }
            message?.let {
                _snackbarMessage.value = Event(it)
                _isBackupRequested.value = false
            }
        }
    }

    fun concludeRestore(listOfInfos: List<WorkInfo>) {
        if (listOfInfos.isNotEmpty() && _isRestoreRequested.value!!) {
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
                _isRestoreRequested.value = false
            }

        }
    }

    fun changeDbLanguage(language: String) {
        val inputLanguage = workDataOf(KEY_LANGUAGE_DB to language)

        val backup = OneTimeWorkRequest.from(BackupDatabaseWorker::class.java)
        val prepare = OneTimeWorkRequestBuilder<ChangeLanguagePreparationsWorker>()
            .setInputData(inputLanguage)
            .build()
        val insertFood = OneTimeWorkRequestBuilder<FoodDatabaseWorker>()
            .setInputData(inputLanguage)
            .build()
        val restore = OneTimeWorkRequest.from(RestoreDatabaseWorker::class.java)

        val changeLangChain = workManager
            .beginUniqueWork(BackupDatabaseWorker.NAME, ExistingWorkPolicy.KEEP, backup)
            .then(prepare)
            .then(insertFood)
            .then(restore)
        changeLangChain.enqueue()

        changeLangStatus = changeLangChain.workInfosLiveData
        _isChangeLangRequested.value = true
    }

    fun changeLangStateAltered(listOfInfos: List<WorkInfo>) {
        if (listOfInfos.isNotEmpty() && listOfInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
            _snackbarMessage.value = Event(R.string.message_data_db_lang)
            _isChangeLangRequested.value = false
        } else if (listOfInfos.any { it.state == WorkInfo.State.FAILED }) {
            _snackbarMessage.value = Event(R.string.message_data_db_lang_fail)
            _isChangeLangRequested.value = false
        }
    }
}