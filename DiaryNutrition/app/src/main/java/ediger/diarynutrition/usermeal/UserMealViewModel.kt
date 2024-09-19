package ediger.diarynutrition.usermeal

import android.app.Application
import androidx.lifecycle.*
import com.google.gson.Gson
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.KEY_MEAL_ORDER
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.data.repositories.MealRepository
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserMealViewModel(val app: Application): AndroidViewModel(app) {

    private val mealRepository: MealRepository = (app as AppContext).mealRepository

    private val mealsSortOrder: List<Int>
        get() {
            val jsonString = PreferenceHelper.getValue(KEY_MEAL_ORDER, String::class.java, "[]")
            return Gson().fromJson(jsonString, Array<Int>::class.java).toList()
        }

    val mealList: LiveData<List<Meal>> = mealRepository.mealsFlow.map { list ->
        if (mealsSortOrder.isEmpty()) {
            return@map list
        } else {
            val sortedList: MutableList<Meal> = mutableListOf()
            mealsSortOrder.forEach { id ->
                list.find { it.id == id}?.let { sortedList.add(it) }
            }
            if (mealsSortOrder.size < list.size) {
                val startIndex = mealsSortOrder.size
                list.subList(startIndex, list.size).forEach { sortedList.add(it) }
            }
            return@map sortedList
        }
    }.asLiveData()

    fun insertMeal(name: String) = viewModelScope.launch {
        mealRepository.insertMeal(
                meal = Meal(name, 1)
        )
    }

}