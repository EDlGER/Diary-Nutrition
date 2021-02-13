package ediger.diarynutrition.data.source.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meal")
data class Meal(var name: String,
           @field:ColumnInfo(defaultValue = "0") var user: Int = 0) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    override fun toString(): String = name
}