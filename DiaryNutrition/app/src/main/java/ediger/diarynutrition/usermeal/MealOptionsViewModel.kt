package ediger.diarynutrition.usermeal

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.data.repositories.MealRepository
import ediger.diarynutrition.data.source.entities.Meal
import kotlinx.coroutines.launch

class MealOptionsViewModel(val app: Application): AndroidViewModel(app) {

    private val mealRepository: MealRepository = (app as AppContext).mealRepository

    var selectedMeal: Meal? = null

    fun setMealId(id: Int) = viewModelScope.launch {
        selectedMeal = mealRepository.getMeal(id)
    }

    fun deleteSelectedMeal() {
        selectedMeal?.let { meal ->
            viewModelScope.launch { mealRepository.deleteMeal(meal.id) }
        }
    }

    fun editMeal(newName: String) = viewModelScope.launch {
        selectedMeal?.let { meal ->
            mealRepository.updateMeal(meal.apply { name = newName })
        }
    }


    // TODO: Hide/Show
    // meal.user = 2 OR use preferences for hiding      (?)
    fun hideMeal(id: Int) {  }

    fun showMeal() {

    }

}