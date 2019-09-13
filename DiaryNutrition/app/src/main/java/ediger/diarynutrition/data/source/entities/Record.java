package ediger.diarynutrition.data.source.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//@Entity(tableName = "record",
//        indices = {@Index("meal_id"), @Index("food_id")},
//        foreignKeys = {
//                @ForeignKey(entity = Food.class,
//                    parentColumns = "_id",
//                    childColumns = "food_id",
//                    onDelete = ForeignKey.CASCADE
//                ),
//                @ForeignKey(entity = Meal.class,
//                    parentColumns = "_id",
//                    childColumns = "meal_id",
//                    onDelete = ForeignKey.CASCADE
//                )
//        })
@Entity(tableName = "record")
public class Record {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "meal_id")
    private int mealId;

    @ColumnInfo(name = "food_id")
    private int foodId;

    private int serving;

    @ColumnInfo(name = "record_datetime")
    private long datetime;

    public Record(int mealId, int foodId, int serving, long datetime) {
        this.mealId = mealId;
        this.foodId = foodId;
        this.serving = serving;
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getFoodId() {
        return foodId;
    }

    public int getServing() {
        return serving;
    }

    public void setServing(int serving) {
        this.serving = serving;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
