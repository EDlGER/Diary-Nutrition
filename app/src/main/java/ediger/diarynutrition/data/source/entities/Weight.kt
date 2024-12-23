package ediger.diarynutrition.data.source.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight")
class Weight(var amount: Float, var datetime: Long) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}