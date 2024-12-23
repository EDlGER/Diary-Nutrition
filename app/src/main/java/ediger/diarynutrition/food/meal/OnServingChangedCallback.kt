package ediger.diarynutrition.food.meal

fun interface OnServingChangedCallback {

    fun onChange(foodId: Int, serving: String)

}