package ediger.diarynutrition.diary

import android.app.Application
import androidx.lifecycle.*
import com.google.gson.Gson
import ediger.diarynutrition.*
import ediger.diarynutrition.data.source.entities.*
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

    private val mealsSortOrder: List<Int>
        get() {
            val jsonString = PreferenceHelper.getValue(KEY_MEAL_ORDER, String::class.java, "[]")
            return Gson().fromJson(jsonString, Array<Int>::class.java).toList()
        }

    private val hiddenMealsList: MutableList<Int>
        get() {
            val hiddenMealsString = PreferenceHelper.getValue(KEY_MEAL_HIDDEN, String::class.java, "[]")
            return Gson().fromJson(hiddenMealsString, Array<Int>::class.java).toMutableList()
        }

    init {
        _recordsList.addSource(_date) { date: Calendar ->
            viewModelScope.launch {
                val hideEmptyUserMeal: Boolean =
                        PreferenceHelper.getValue(KEY_MEALS_USER_HIDE, Boolean::class.javaObjectType, true)
                val originalList = recordRepository.getRecords(date)
                val filteredList = reorderList(originalList, mealsSortOrder).filter {
                    !hiddenMealsList.contains(it.meal?.id)
                            && !(hideEmptyUserMeal && it.meal?.user == 1 && it.records?.isEmpty() ?: true)
                }
                _recordsList.setValue(filteredList)
            }
        }
        daySummary = Transformations.switchMap(_date) { date: Calendar -> summaryRepository.getDaySummary(date) }
        water = Transformations.switchMap(_date) { date: Calendar -> waterRepository.getWaterSum(date) }
    }

    private fun reorderList(list: List<MealAndRecords>, mealsOrder: List<Int>): List<MealAndRecords> {
        return if (mealsOrder.isEmpty()) {
            list
        } else {
            val sortedList: MutableList<MealAndRecords> = mutableListOf()

            mealsOrder.forEach { id ->
                list.find { it.meal?.id == id }?.let { sortedList.add(it) }
            }
            if (mealsOrder.size < list.size) {
                val startIndex = mealsOrder.size
                list.subList(startIndex, list.size).forEach { sortedList.add(it) }
            }
            sortedList
        }
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

    fun updateDate() {
        _date.value = _date.value
    }

    private fun showSnackbarMessage(message: Int) {
        snackbarMessage.value = message
    }
}