package ediger.diarynutrition.usermeal

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.data.repositories.MealRepository
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.launch

class MealOptionsViewModel(val app: Application): AndroidViewModel(app) {

    private val mealRepository: MealRepository = (app as AppContext).mealRepository

    var selectedMeal: Meal? = null

    private val _isUserMeal = MutableLiveData(false)
    val isUserMeal: LiveData<Boolean> = _isUserMeal

    val shouldDismiss = MutableLiveData(false)

    fun setMealId(id: Int) = viewModelScope.launch {
        selectedMeal = mealRepository.getMeal(id)
        _isUserMeal.value = selectedMeal?.user == 1
    }

    // TODO: What to do with previous records under this meal?
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


    // TODO: Hide/Show
    // meal.user = 2 OR use preferences for hiding      (?)
    fun hideMeal(id: Int) {  }

    fun showMeal() {

    }

}