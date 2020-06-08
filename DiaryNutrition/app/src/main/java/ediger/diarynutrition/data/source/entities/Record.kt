package ediger.diarynutrition.data.source.entities

import androidx.room.*

@Entity(tableName = "record",
        indices = [Index("meal_id"), Index("food_id")],
        foreignKeys = [
            ForeignKey(entity = Food::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("food_id"),
                    onDelete = ForeignKey.RESTRICT),
            ForeignKey(entity = Meal::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("meal_id"),
                    onDelete = ForeignKey.SET_DEFAULT)])
class Record(
        @field:ColumnInfo(name = "meal_id") var mealId: Int,
        @field:ColumnInfo(name = "food_id") var foodId: Int,
        var serving: Int,
        var datetime: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}