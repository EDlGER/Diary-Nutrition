package ediger.diarynutrition.data;

import androidx.room.Embedded;

public class RecordWithFoodAndMeal {

    @Embedded(prefix = "record.")
    Record record;

    @Embedded(prefix = "food.")
    Food food;

    @Embedded(prefix = "meal.")
    Meal meal;

}
