package ediger.diarynutrition.data;

import androidx.room.Embedded;

public class RecordWithFoodAndMeal {

    @Embedded(prefix = "record_")
    public Record record;

    @Embedded(prefix = "food_")
    public Food food;

    @Embedded(prefix = "meal_")
    public Meal meal;

}
