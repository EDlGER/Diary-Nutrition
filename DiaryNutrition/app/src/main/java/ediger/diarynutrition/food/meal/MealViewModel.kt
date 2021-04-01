package ediger.diarynutrition.food.meal

import android.app.Application
import androidx.lifecycle.*
import com.google.gson.Gson
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.KEY_MEAL_HIDDEN
import ediger.diarynutrition.KEY_MEAL_ORDER
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.data.repositories.FoodRepository
import ediger.diarynutrition.data.repositories.MealRepository
import ediger.diarynutrition.data.repositories.RecordRepository
import ediger.diarynutrition.data.source.entities.*
import kotlinx.coroutines.launch
import java.util.*

class MealViewModel(val app: Application) : AndroidViewModel(app) {

    private val mealRepository: MealRepository = (app as AppContext).mealRepository

    private val foodRepository: FoodRepository = (app as AppContext).foodRepository

    private val recordRepository: RecordRepository = (app as AppContext).recordRepository

    private val _recordAndFoodList: MutableLiveData<MutableList<RecordAndFood>> = MutableLiveData(mutableListOf())
    val recordAndFoodList: LiveData<MutableList<RecordAndFood>> = _recordAndFoodList

    val totalMacro = MediatorLiveData<Food>()

    private val _mealList: MutableLiveData<List<Meal>> = MutableLiveData(emptyList())
    val mealList: LiveData<List<Meal>> = _mealList

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

    private val mealsSortOrder: List<Int>
        get() {
            val jsonString = PreferenceHelper.getValue(KEY_MEAL_ORDER, String::class.java, "[]")
            return Gson().fromJson(jsonString, Array<Int>::class.java).toList()
        }

    private val hiddenMealsList: MutableList<Int>
        get() {
            val hiddenMealsString = PreferenceHelper.getValue(KEY_MEAL_HIDDEN, String::class.java, "[]")
            return Gson().fromJson(hiddenMealsString, Array<Int>::class.java).toMutableList()
        }

    init {
        viewModelScope.launch {
            _mealList.value = mealRepository.getMeals()
                    .reorderList(mealsSortOrder)
                    .filter { meal -> !hiddenMealsList.contains(meal.id)}
        }
        totalMacro.addSource(_recordAndFoodList) { totalMacro.value = getUpdatedMacro() }
    }

    fun getFood(id: Int) = foodRepository.getFood(id)

    fun foodSelected(food: Food?) {
        food?.let {
            // Check if that food is already selected
            _recordAndFoodList.value?.let { list ->
                if (list.map { it.food }.contains(food)) {
                    return
                }
            }

            val recordAndFood = RecordAndFood(
                    Record(selectedMealId, food.id, 100, selectedTime),
                    food
            )
            _recordAndFoodList.value?.add(recordAndFood)
            _recordAndFoodList.value = _recordAndFoodList.value
        }
    }

    fun updateServing(foodId: Int, serving: Int) {
        _recordAndFoodList.value
                ?.findLast { it.food?.id == foodId }
                ?.apply { record?.serving = serving }
        _recordAndFoodList.value = _recordAndFoodList.value
    }

    fun addRecords() = viewModelScope.launch {
        _recordAndFoodList.value?.let { list ->
            recordRepository.addRecords(list.mapNotNull { it.record })
        }
    }

    fun removeFood(id: Int) {
        _recordAndFoodList.value?.let { list ->
            list.remove(
                    list.firstOrNull { it.food?.id == id }
            )
        }
        _recordAndFoodList.value = _recordAndFoodList.value
    }

    fun updateMealId() {
        selectedMealId = chooseMealId()
    }

    private fun List<Meal>.reorderList(mealsOrder: List<Int>): List<Meal> {
        return if (mealsOrder.isEmpty()) {
            this
        } else {
            val sortedList: MutableList<Meal> = mutableListOf()

            mealsOrder.forEach { id ->
                this.find { it.id == id }?.let { sortedList.add(it) }
            }
            if (mealsOrder.size < this.size) {
                val startIndex = mealsOrder.size
                this.subList(startIndex, this.size).forEach { sortedList.add(it) }
            }
            sortedList
        }
    }

    private fun getUpdatedMacro(): Food {
        var cal = 0f
        var prot = 0f
        var fat = 0f
        var carbo = 0f

        _recordAndFoodList.value?.forEach { it ->
            var serving = 100
            it.record?.let { record ->  serving = record.serving }
            it.food?.let { food ->
                cal += food.cal * serving / 100
                prot += food.prot * serving / 100
                fat += food.fat * serving / 100
                carbo += food.carbo * serving / 100
            }
        }
        return Food("", cal, prot, fat, carbo)
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
        val mealId = when (hour) {
            in 3..8  -> 1
            in 9..11 -> 2
            in 12..14 -> 3
            in 15..17 -> 4
            else -> 5
        }
        _mealList.value
                ?.map { it.id }
                ?.let { list ->
                    if (list.isNotEmpty() && !list.contains(mealId)) {
                        return list.last()
                    }
                }

        return mealId
    }

}