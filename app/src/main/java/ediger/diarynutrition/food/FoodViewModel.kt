package ediger.diarynutrition.food

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import ediger.diarynutrition.*
import kotlinx.coroutines.flow.Flow
import ediger.diarynutrition.data.repositories.FoodRepository
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.launch


class FoodViewModel(app: Application) : AndroidViewModel(app) {

    private var repository: FoodRepository = (app as AppContext).foodRepository

    private val _queryValue: MutableLiveData<String> = MutableLiveData()
    val queryValue: LiveData<String> = _queryValue

    private var searchResult: Flow<PagingData<Food>>? = null

    val isSheetActive = MutableLiveData(false)

    val isAdVisible = MutableLiveData(true)

    var userFoodConstraint: Int = PreferenceHelper
            .getValue(KEY_USER_FOOD_CONSTRAINT, Int::class.javaObjectType, 5)
        set(value) {
            if (field != value) {
                PreferenceHelper.setValue(KEY_USER_FOOD_CONSTRAINT, value)
            }
            field = value
        }

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    init {
        _queryValue.value = ""
        if (userFoodConstraint !in 0..10) {
            userFoodConstraint = 0
        }
    }

    fun searchFood(foodVariance: FoodVariance): Flow<PagingData<Food>>? {
        val query = queryValue.value ?: return searchResult

        val newSearchResult = when (foodVariance) {

            FoodVariance.ALL -> {
                if (query.isEmpty())
                    repository.getPopularFood()
                else
                    repository.searchAllFood(query)
            }

            FoodVariance.FAVORITES -> repository.searchFavoriteFood(query)

            FoodVariance.USER -> repository.searchUserFood(query)

        }.cachedIn(viewModelScope)

        searchResult = newSearchResult

        return newSearchResult
    }

    fun setFavoriteFood(id: Int, favorite: Boolean) = viewModelScope.launch {
        repository.setFavorite(id, favorite)
        if (favorite) showSnackbarMessage(R.string.message_favorite_add)
    }

    fun deleteFood(id: Int) = viewModelScope.launch {
        repository.deleteFood(id)
    }

    fun addFood(name: String?, cal: Float?, prot: Float?, fat: Float?, carbo: Float?, gi: Int?): Boolean {
        if (name.isNullOrBlank() || cal == null || prot == null || fat == null || carbo == null) {
            showSnackbarMessage(R.string.message_dialog_food_empty)
            return false
        }
        viewModelScope.launch {
            val food = Food(name, cal, prot, fat, carbo).apply {
                this.gi = gi ?: 0
                user = 1
            }
            repository.addFood(food)
        }
        userFoodConstraint--
        return true
    }

    fun updateFood(food: Food?) =
        food?.let {
            viewModelScope.launch { repository.updateFood(food) }
        }


    fun getFood(id: Int?) = id?.let { repository.getFood(id) }

    fun setQueryValue(query: String) {
        _queryValue.value = query
    }

    fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    companion object {
        const val FOOD_ID = "foodId"
    }

}