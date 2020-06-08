package ediger.diarynutrition.data.source.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.opencsv.bean.CsvBindByPosition

@Entity(tableName = "food")
class Food(
        var name: String = "",
        var cal: Float = 0f,
        var prot: Float = 0f,
        var fat: Float = 0f,
        var carbo: Float = 0f) {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(defaultValue = "0")
    var favorite = 0

    @ColumnInfo(defaultValue = "0")
    var user = 0

    @ColumnInfo(defaultValue = "0")
    var verified = 0

    @ColumnInfo(defaultValue = "0")
    var gi = 0
}