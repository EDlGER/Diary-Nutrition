package ediger.diarynutrition.data.source.model

import ediger.diarynutrition.data.source.entities.*

data class JsonDatabaseBackup(
        val recordList: List<Record>,
        val foodList: List<Food>,
        val mealList: List<Meal>,
        val waterList: List<Water>,
        val weightList: List<Weight>
)