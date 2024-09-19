package ediger.diarynutrition.usermeal

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.KEY_MEAL_HIDDEN
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.data.repositories.MealRepository
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.launch

class MealOptionsViewModel(val app: Application): AndroidViewModel(app) {

    private val mealRepository: MealRepository = (app as AppContext).mealRepository

    var selectedMeal: Meal? = null

    private val _isUserMeal = MutableLiveData(false)
    val isUserMeal: LiveData<Boolean> = _isUserMeal

    val shouldDismiss = MutableLiveData(false)

    private val hiddenMealsList: MutableList<Int> =
            Gson().fromJson(
                    PreferenceHelper.getValue(KEY_MEAL_HIDDEN, String::class.java, "[]"),
                    Array<Int>::class.java
            ).toMutableList()

    private val _isHidden = MutableLiveData(hiddenMealsList.contains(selectedMeal?.id))
    val isHidden: LiveData<Boolean> = _isHidden

    fun setMealId(id: Int) = viewModelScope.launch {
        selectedMeal = mealRepository.getMeal(id)

        _isUserMeal.value = selectedMeal?.user == 1
        _isHidden.value = hiddenMealsList.contains(selectedMeal?.id)
    }

    fun deleteSelectedMeal() {
        selectedMeal?.let { meal ->
            viewModelScope.launch {
                mealRepository.deleteMeal(meal.id)
                shouldDismiss.value = true
            }
        }
    }

    fun editMeal(newName: String) = viewModelScope.launch {
        selectedMeal?.let { meal ->
            mealRepository.updateMeal(meal.apply { name = newName })
            shouldDismiss.value = true
        }
    }

    fun hideMeal() {
        selectedMeal?.let { meal ->
            if (!hiddenMealsList.contains(meal.id)) {
                hiddenMealsList.add(meal.id)
            }
            PreferenceHelper.setValue(KEY_MEAL_HIDDEN, Gson().toJson(hiddenMealsList))
            shouldDismiss.value = true
        }
    }

    fun showMeal() {
        selectedMeal?.let { meal ->
            hiddenMealsList.remove(meal.id)

            PreferenceHelper.setValue(KEY_MEAL_HIDDEN, Gson().toJson(hiddenMealsList))
            shouldDismiss.value = true
        }
    }

}