package ediger.diarynutrition.data.source.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water")
class Water(val amount: Int, val datetime: Long) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}