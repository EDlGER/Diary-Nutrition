package ediger.diarynutrition.data;

import androidx.room.Embedded;

public class RecordWithFoodAndMeal {

    @Embedded
    Record record;

    @Embedded
    Food food;

    @Embedded
    Meal meal;

}
