package ediger.diarynutrition.summary

import android.app.Application
import androidx.lifecycle.*
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.data.source.entities.Water
import kotlinx.coroutines.launch
import java.util.*

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val summaryRepository = (application as AppContext).summaryRepository

    private var numOfDays = 1

    private val sinceDate = MutableLiveData<Calendar>()

    private val _summary = MediatorLiveData<List<Summary>>()
    val summary: LiveData<List<Summary>> = _summary

    private val _water = MediatorLiveData<List<Water>>()
    val water: LiveData<List<Water>> = _water

    fun setPeriod(since: Calendar, numberOfDays: Int) {
        numOfDays = numberOfDays
        sinceDate.value = since
    }

    init {
        _summary.value = emptyList()
        _summary.addSource(sinceDate) { date: Calendar ->
            viewModelScope.launch {
                _summary.setValue(summaryRepository.getSummary(date, numOfDays))
            }
        }

        _water.value = emptyList()
        _water.addSource(sinceDate) { date: Calendar ->
            viewModelScope.launch {
                _water.setValue(summaryRepository.getWaterSummary(date, numOfDays))
            }
        }
    }
}