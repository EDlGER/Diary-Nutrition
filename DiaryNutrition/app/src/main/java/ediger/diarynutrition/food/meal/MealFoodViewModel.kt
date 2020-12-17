package ediger.diarynutrition.food.meal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.data.DiaryRepository
import ediger.diarynutrition.data.source.entities.RecordAndFood
import java.util.*

class MealFoodViewModel(val app: Application) : AndroidViewModel(app) {

    private var repository: DiaryRepository = (app as AppContext).repository

    private val _recordAndFoodList: MutableLiveData<MutableList<RecordAndFood>> = MutableLiveData(mutableListOf())
    val recordAndFoodList: LiveData<MutableList<RecordAndFood>> = _recordAndFoodList

    var selectedTime = System.currentTimeMillis()
        set(value) {
            field = value
            updateTime()
        }

    var selectedMealId: Int = chooseMealId()
        set(value) {
            field = value
            updateMeal()
        }


    fun updateServing(foodId: Int, serving: Int) {
        _recordAndFoodList.value
                ?.findLast { it.food?.id == foodId }
                ?.apply { record?.serving = serving }
        _recordAndFoodList.value = _recordAndFoodList.value
    }

    private fun updateTime() = _recordAndFoodList.value?.forEach {
            it.record?.datetime = selectedTime
        }

    private fun updateMeal() = _recordAndFoodList.value?.forEach {
            it.record?.mealId = selectedMealId
        }

    private fun chooseMealId(): Int {
        val hour = Calendar.getInstance()
                .apply { timeInMillis = selectedTime }
                .get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 3..8 -> 1
            in 9..11 -> 2
            in 12..14 -> 3
            in 15..17 -> 4
            else -> 5
        }
    }
}