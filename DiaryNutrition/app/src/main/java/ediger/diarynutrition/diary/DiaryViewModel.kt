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

    private val _date = MutableLiveData(Calendar.getInstance())
    var date: Calendar?
        get() = _date.value
        set(calendar) { _date.value = calendar }

    private val _isRemaining = MutableLiveData(true)
    val isRemaining: LiveData<Boolean> = _isRemaining

    @JvmField
    val water: LiveData<Water>

    val daySummary: LiveData<Summary>

    val snackbarMessage = SnackbarMessage()

    init {
        mRepository = (app as AppContext).repository
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

    fun switchIsRemaining() {
        _isRemaining.value?.let { _isRemaining.value = !it }
    }

    fun pasteChildren() {
        _date.value = _date.value
        showSnackbarMessage(R.string.message_record_meal_insert)
    }

    private fun showSnackbarMessage(message: Int) {
        snackbarMessage.value = message
    }

}