package ediger.diarynutrition.diary.water

import android.app.Application
import androidx.lifecycle.*
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.data.source.entities.Water
import kotlinx.coroutines.launch
import java.util.*

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val waterRepository = (application as AppContext).waterRepository

    private val date = MutableLiveData<Calendar>()

    val water: LiveData<List<Water>>

    var addTime: Calendar = Calendar.getInstance()

    init {
        date.value = Calendar.getInstance()
        water = date.switchMap { day: Calendar -> waterRepository.getWaterList(day) }
    }

    fun setDate(day: Calendar) {
        date.value = day
    }

    fun addWater(amount: Int) = viewModelScope.launch {
        waterRepository.addWater(
                Water(amount, addTime.timeInMillis)
        )
    }

    fun deleteWater(id: Int) = viewModelScope.launch { waterRepository.deleteWater(id) }
}