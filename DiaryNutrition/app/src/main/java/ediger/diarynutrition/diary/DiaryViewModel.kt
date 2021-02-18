package ediger.diarynutrition.diary

import android.app.Application
import androidx.lifecycle.*
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.R
import ediger.diarynutrition.data.DiaryRepository
import ediger.diarynutrition.data.source.entities.MealAndRecords
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.data.source.entities.Water
import ediger.diarynutrition.objects.SnackbarMessage
import kotlinx.coroutines.launch
import java.util.*

class DiaryViewModel(app: Application) : AndroidViewModel(app) {

    private val recordRepository = (app as AppContext).recordRepository

    private val summaryRepository = (app as AppContext).summaryRepository

    private val waterRepository = (app as AppContext).waterRepository

    private val _recordsList: MediatorLiveData<List<MealAndRecords>?> = MediatorLiveData()
    val recordsList: LiveData<List<MealAndRecords>?> = _recordsList

    private val _date = MutableLiveData(Calendar.getInstance())
    var date: Calendar?
        get() = _date.value
        set(calendar) {
            _date.value = calendar?.apply {
                set(Calendar.HOUR_OF_DAY, _date.value?.get(Calendar.HOUR_OF_DAY) ?: 0)
                set(Calendar.MINUTE, _date.value?.get(Calendar.MINUTE) ?: 0)
            }
        }

    private val _isRemaining = MutableLiveData(true)
    val isRemaining: LiveData<Boolean> = _isRemaining

    var selectedRecordId = -1

    var recordBuffer: MutableList<Record> = mutableListOf()

    @JvmField
    val water: LiveData<Water>

    val daySummary: LiveData<Summary>

    val snackbarMessage = SnackbarMessage()

    init {
        _recordsList.addSource(_date) { date: Calendar ->
            viewModelScope.launch {
                _recordsList.setValue(recordRepository.getRecords(date))
            }
        }

        daySummary = Transformations.switchMap(_date) { date: Calendar -> summaryRepository.getDaySummary(date) }
        water = Transformations.switchMap(_date) { date: Calendar -> waterRepository.getWaterSum(date) }
    }

    fun addRecord(record: Record) = viewModelScope.launch {
        recordRepository.addRecord(record)
    }

    fun delRecord(id: Int) = viewModelScope.launch {
        recordRepository.deleteRecord(id)
    }

    fun updateRecord(serving: Int) = viewModelScope.launch {
        // Find record by selected id in the list, change and pass to the repository
        _recordsList.value?.let { list ->
            list.flatMap { it.records?.map { recordAndFood ->  recordAndFood.record } ?: return@launch }
                    .find { it?.id == selectedRecordId }
                    ?.apply { this.serving = serving }
                    ?.let {
                        recordRepository.updateRecord(it)
                        _date.value = _date.value
                    }
        }
    }

    fun updateRecordTime(time: Long) = viewModelScope.launch {
        _recordsList.value?.let { list ->
            list.flatMap { it.records?.map { recordAndFood ->  recordAndFood.record } ?: return@launch }
                    .find { it?.id == selectedRecordId }
                    ?.apply { this.datetime = time }
                    ?.let {
                        recordRepository.updateRecord(it)
                        _date.value = _date.value
                    }
        }
    }

    fun copyMeal(mealId: Int) {
        recordBuffer.clear()
        _recordsList.value?.let { list ->
            list.find { it.meal?.id == mealId }?.records
                    ?.mapNotNull { it.record }
                    ?.let { records ->
                        recordBuffer.addAll(records)
                        showSnackbarMessage(R.string.message_record_meal_copy)
                    }
        }
    }

    fun pasteMeal(mealId: Int) = viewModelScope.launch {
        if (recordBuffer.isEmpty()) {
            showSnackbarMessage(R.string.message_record_meal_insert_fail)
        }
        date?.let { date ->
            recordBuffer.forEach {
                val oldDate = Calendar.getInstance().apply { timeInMillis = it.datetime }
                val newDate = (date.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, oldDate[Calendar.HOUR_OF_DAY])
                    set(Calendar.MINUTE, oldDate[Calendar.MINUTE])
                }
                it.datetime = newDate.timeInMillis
                it.mealId = mealId
                it.id = 0
            }
            recordRepository.addRecords(recordBuffer)

            recordBuffer.clear()

            _date.value = _date.value
        }
    }

    fun addWater(water: Water) = viewModelScope.launch { waterRepository.addWater(water) }

    fun switchIsRemaining() {
        _isRemaining.value?.let { _isRemaining.value = !it }
    }

    private fun showSnackbarMessage(message: Int) {
        snackbarMessage.value = message
    }
}