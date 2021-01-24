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

    // TODO: delete
    private val mRepository: DiaryRepository

    private val _recordsList: MediatorLiveData<List<MealAndRecords>?> = MediatorLiveData()
    val recordsList: LiveData<List<MealAndRecords>?> = _recordsList

    private val _date = MutableLiveData<Calendar>()
    var date: Calendar?
        get() = _date.value
        set(calendar) {
            _date.value = calendar
        }

    @JvmField
    val water: LiveData<Water>

    val daySummary: LiveData<Summary>

    val snackbarMessage = SnackbarMessage()

    init {
        mRepository = (app as AppContext).repository

        _date.value = Calendar.getInstance()

        //mRecords.value = null

        _recordsList.addSource(_date) { date: Calendar ->
            viewModelScope.launch {
                _recordsList.setValue(recordRepository.getRecords(date))
            }
        }

        daySummary = Transformations.switchMap(_date) { date: Calendar -> mRepository.getDaySummary(date) }
        water = Transformations.switchMap(_date) { date: Calendar -> mRepository.getWaterSum(date) }
    }

    fun addRecord(record: Record) = viewModelScope.launch {
        recordRepository.addRecord(record)
    }

    fun delRecord(id: Int) = viewModelScope.launch {
        recordRepository.deleteRecord(id)
    }

    fun addWater(water: Water) = mRepository.addWater(water)

    fun pasteChildren() {
        _date.value = _date.value
        showSnackbarMessage(R.string.message_record_meal_insert)
    }

    private fun showSnackbarMessage(message: Int) {
        snackbarMessage.value = message
    }

}