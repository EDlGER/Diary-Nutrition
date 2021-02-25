package ediger.diarynutrition.weight

import android.app.Application
import androidx.lifecycle.*
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.KEY_WEIGHT
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.data.source.entities.Weight
import kotlinx.coroutines.launch
import java.util.*

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val weightRepository = (application as AppContext).weightRepository

    val weight: LiveData<List<Weight>> = weightRepository.getWeight()

    private val mDateSince = MutableLiveData<Calendar>()

    val weightSinceDate: LiveData<List<Weight>>

    init {
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -8) }
        mDateSince.value = from
        weightSinceDate = Transformations
                .switchMap(mDateSince) { date: Calendar -> weightRepository.getWeight(date) }
    }

    fun addWeight(weight: Weight) = viewModelScope.launch {
        val day = Calendar.getInstance().apply { timeInMillis = weight.datetime }
        val lastWeight = weightRepository.getWeightForDay(day)

        if (lastWeight == null) {
            weightRepository.addWeight(weight)
        } else {
            lastWeight.amount = weight.amount
            weightRepository.updateWeight(lastWeight)
        }

        //Updates for use of NutritionProgram
        PreferenceHelper.setValue(KEY_WEIGHT, weight.amount)
    }

    fun deleteWeight(id: Int) = viewModelScope.launch { weightRepository.deleteWeight(id) }

    fun setDate(since: Calendar) {
        mDateSince.value = since
    }
}