package ediger.diarynutrition.food.meal

interface OnServingChangedCallback {

    fun onChange(foodId: Int, serving: String)

}