package ediger.diarynutrition.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.dao.*
import ediger.diarynutrition.data.source.entities.*
import ediger.diarynutrition.DATABASE_NAME
import ediger.diarynutrition.DATABASE_VERSION
import ediger.diarynutrition.DEFAULT_LANGUAGE
import ediger.diarynutrition.util.NutritionProgramUtils
import ediger.diarynutrition.workers.FoodDatabaseWorker
import kotlinx.coroutines.*
import java.util.*

@Database(entities = [Record::class, Food::class, Meal::class, Water::class, Weight::class],
        version = DATABASE_VERSION, exportSchema = true)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao
    abstract fun recordDao(): RecordDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        @Volatile private var instance: DiaryDatabase? = null

        fun getInstance(context: Context): DiaryDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(appContext: Context): DiaryDatabase {
            return Room.databaseBuilder(appContext, DiaryDatabase::class.java, DATABASE_NAME)
                    .addCallback(object: Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            CoroutineScope(Dispatchers.IO).launch {
                                launch { populateMeals(appContext) }.join()
                            }
                            val language = defineDbLanguage()
                            val insertFoodRequest = OneTimeWorkRequestBuilder<FoodDatabaseWorker>()
                                .setInputData(workDataOf(KEY_LANGUAGE_DB to language))
                                .build()
                            WorkManager.getInstance(appContext).enqueue(insertFoodRequest)
                        }
                    })
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
        }

        private fun defineDbLanguage() = when (Locale.getDefault().language) {
            "ru", "uk" -> "ru"
            else -> DEFAULT_LANGUAGE
        }

        private suspend fun populateMeals(appContext: Context) {
            val meals = appContext.resources.getStringArray(R.array.meals)
                .map { Meal(it) }
            instance?.mealDao()?.populateMeals(meals)
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                //food table
                db.execSQL("CREATE TABLE food_new (id INTEGER NOT NULL," +
                        "name TEXT NOT NULL, " +
                        "cal REAL NOT NULL, carbo REAL NOT NULL, " +
                        "prot REAL NOT NULL, fat REAL NOT NULL, " +
                        "favorite INTEGER DEFAULT 0 NOT NULL, " +
                        "user INTEGER DEFAULT 0 NOT NULL, " +
                        "verified INTEGER DEFAULT 0 NOT NULL, " +
                        "gi INTEGER DEFAULT 0 NOT NULL, PRIMARY KEY (id))")
                db.execSQL("CREATE UNIQUE INDEX index_food_name_cal ON food_new(name, cal)")
                db.execSQL("INSERT OR REPLACE INTO food_new (id, name, cal, carbo, prot, fat, " +
                        "favorite, user) SELECT _id, food_name, cal, carbo, prot, fat, " +
                        "favor, usr FROM food")
                db.execSQL("DROP TABLE food")
                db.execSQL("ALTER TABLE food_new RENAME TO food")
                db.execSQL("UPDATE food SET user = 1 WHERE user > 1")

                //meal table
                db.execSQL("CREATE TABLE meal_new (id INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "user INTEGER DEFAULT 0 NOT NULL, PRIMARY KEY (id))")
                db.execSQL("INSERT INTO meal_new (id, name) " +
                        "SELECT _id, name FROM meal")
                db.execSQL("DROP TABLE meal")
                db.execSQL("ALTER TABLE meal_new RENAME TO meal")

                //record table
                db.execSQL("CREATE TABLE record_new (id INTEGER NOT NULL, " +
                        "food_id INTEGER NOT NULL REFERENCES food(id) ON DELETE RESTRICT ON UPDATE CASCADE," +
                        "meal_id INTEGER DEFAULT 5 NOT NULL REFERENCES meal(id) ON DELETE SET DEFAULT, " +
                        "datetime INTEGER NOT NULL, " +
                        "serving INTEGER NOT NULL, " +
                        "PRIMARY KEY (id))")
                db.execSQL("CREATE INDEX index_record_food_id ON record_new(food_id)")
                db.execSQL("CREATE INDEX index_record_meal_id ON record_new(meal_id)")
                db.execSQL("INSERT INTO record_new (id, food_id, meal_id, datetime, " +
                        "serving) SELECT _id, food_id, meal_id, record_datetime, serving FROM record")
                db.execSQL("DROP TABLE record")
                db.execSQL("ALTER TABLE record_new RENAME TO record")


                //water table
                db.execSQL("CREATE TABLE IF NOT EXISTS water (_id INTEGER, " +
                        "datetime INTEGER, amount INTEGER, PRIMARY KEY(_id))")
                db.execSQL("CREATE TABLE water_new (id INTEGER NOT NULL, " +
                        "datetime INTEGER NOT NULL, amount INTEGER NOT NULL, PRIMARY KEY (id))")
                db.execSQL("INSERT INTO water_new (id, datetime, amount) " +
                        "SELECT _id, datetime, amount FROM water")
                db.execSQL("DROP TABLE water")
                db.execSQL("ALTER TABLE water_new RENAME TO water")


                //weight table
                db.execSQL("CREATE TABLE weight_new (id INTEGER NOT NULL, " +
                        "datetime INTEGER NOT NULL, amount REAL NOT NULL, " +
                        "PRIMARY KEY (id))")
                db.execSQL("INSERT INTO weight_new (id, datetime, amount) " +
                        "SELECT _id, datetime, weight FROM weight")
                db.execSQL("DROP TABLE weight")
                db.execSQL("ALTER TABLE weight_new RENAME TO weight")

                db.execSQL("DROP TABLE user")
                db.execSQL("DROP TABLE date")

                NutritionProgramUtils.setToDefault()
            }
        }
    }
}