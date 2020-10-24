package ediger.diarynutrition.food

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.Event
import ediger.diarynutrition.R
import ediger.diarynutrition.data.FoodRepository
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.launch


class FoodViewModel(app: Application): AndroidViewModel(app) {

    private var repository: FoodRepository = (app as AppContext).foodRepository

    private val _queryValue: MutableLiveData<String> = MutableLiveData()
    val queryValue: LiveData<String> = _queryValue

    private var searchResult: Flow<PagingData<Food>>? = null

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    init {
        _queryValue.value = ""
    }

    fun searchFood(foodVariance: FoodVariance): Flow<PagingData<Food>>? {
        val query = queryValue.value ?: return searchResult

        val newSearchResult = when (foodVariance) {

            FoodVariance.ALL -> repository.searchAllFood(query)

            FoodVariance.FAVORITES -> repository.searchFavoriteFood(query)

            FoodVariance.USER -> repository.searchUserFood(query)

        }.cachedIn(viewModelScope)

        searchResult = newSearchResult

        return newSearchResult
    }

    fun setFavoriteFood(id: Int, favorite: Boolean) = viewModelScope.launch {
        repository.setFavorite(id, favorite)
        if (favorite) {
            showSnackbarMessage(R.string.message_favorite_add)
        }
    }

    fun deleteFood(id: Int) = viewModelScope.launch {
        repository.deleteFood(id)
    }

    fun setQueryValue(query: String) {
        _queryValue.value = query
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

}