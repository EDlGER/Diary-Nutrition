package ediger.diarynutrition.data.source.entities

import androidx.room.Embedded

data class RecordAndFood(
    @Embedded
    var record: Record?,
    @Embedded(prefix = "f_")
    var food: Food?
)