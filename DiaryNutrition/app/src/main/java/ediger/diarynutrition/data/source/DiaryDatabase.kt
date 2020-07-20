package ediger.diarynutrition.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.KEY_LOCAL_DB_VERSION
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.dao.*
import ediger.diarynutrition.data.source.entities.*
import ediger.diarynutrition.DATABASE_NAME
import ediger.diarynutrition.workers.FoodDatabaseWorker
import kotlinx.coroutines.*
import java.util.*

@Database(entities = [Record::class, Food::class, Meal::class, Water::class, Weight::class],
        version = 2, exportSchema = true)
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
                            PreferenceHelper.setValue(KEY_LOCAL_DB_VERSION, 1)
                            defineDbLanguage()

                            CoroutineScope(Dispatchers.IO).launch {
                                launch { populateMeals(appContext) }.join()
                            }
                            val request = OneTimeWorkRequestBuilder<FoodDatabaseWorker>().build()
                            WorkManager.getInstance(appContext).enqueue(request)
                        }
                    })
                    .addMigrations(MIGRATION_1_2)
                    .build()
        }

        private fun defineDbLanguage() {
            PreferenceHelper.setValue(
                    KEY_LANGUAGE_DB,
                    when (Locale.getDefault().language) {
                        "ru", "uk" -> "ru"
                        else -> "en"
                    }
            )
        }

        private suspend fun populateMeals(appContext: Context) {
            val meals = listOf(
                    Meal(appContext.resources.getString(R.string.meal1)),
                    Meal(appContext.resources.getString(R.string.meal2)),
                    Meal(appContext.resources.getString(R.string.meal3)),
                    Meal(appContext.resources.getString(R.string.meal4)),
                    Meal(appContext.resources.getString(R.string.meal5))
            )
            instance?.mealDao()?.insertMeals(meals)
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //food table
                database.execSQL("CREATE TABLE food_new (id INTEGER NOT NULL," +
                        "name TEXT NOT NULL, " +
                        "cal REAL NOT NULL, carbo REAL NOT NULL, " +
                        "prot REAL NOT NULL, fat REAL NOT NULL, " +
                        "favorite INTEGER DEFAULT 0 NOT NULL, " +
                        "user INTEGER DEFAULT 0 NOT NULL, " +
                        "verified INTEGER DEFAULT 0 NOT NULL, " +
                        "gi INTEGER DEFAULT 0 NOT NULL, PRIMARY KEY (id))")
                database.execSQL("INSERT INTO food_new (id, name, cal, carbo, prot, fat, " +
                        "favorite, user) SELECT _id, food_name, cal, carbo, prot, fat, " +
                        "favor, usr FROM food")
                database.execSQL("DROP TABLE food")
                database.execSQL("ALTER TABLE food_new RENAME TO food")

                //meal table
                database.execSQL("CREATE TABLE meal_new (id INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "user INTEGER DEFAULT 0 NOT NULL, PRIMARY KEY (id))")
                database.execSQL("INSERT INTO meal_new (id, name) " +
                        "SELECT _id, name FROM meal")
                database.execSQL("DROP TABLE meal")
                database.execSQL("ALTER TABLE meal_new RENAME TO meal")

                //record table
                database.execSQL("CREATE TABLE record_new (id INTEGER NOT NULL, " +
                        "food_id INTEGER NOT NULL REFERENCES food(id) ON DELETE RESTRICT," +
                        "meal_id INTEGER DEFAULT 0 NOT NULL REFERENCES meal(id) ON DELETE SET DEFAULT, " +
                        "datetime INTEGER NOT NULL, " +
                        "serving INTEGER NOT NULL, " +
                        "PRIMARY KEY (id))")
                database.execSQL("CREATE INDEX index_record_food_id ON record_new(food_id)")
                database.execSQL("CREATE INDEX index_record_meal_id ON record_new(meal_id)")
                database.execSQL("INSERT INTO record_new (id, food_id, meal_id, datetime, " +
                        "serving) SELECT _id, food_id, meal_id, record_datetime, serving FROM record")
                database.execSQL("DROP TABLE record")
                database.execSQL("ALTER TABLE record_new RENAME TO record")


                //water table
                database.execSQL("CREATE TABLE IF NOT EXISTS water (_id INTEGER, " +
                        "datetime INTEGER, amount INTEGER, PRIMARY KEY(_id))")
                database.execSQL("CREATE TABLE water_new (id INTEGER NOT NULL, " +
                        "datetime INTEGER NOT NULL, amount INTEGER NOT NULL, PRIMARY KEY (id))")
                database.execSQL("INSERT INTO water_new (id, datetime, amount) " +
                        "SELECT _id, datetime, amount FROM water")
                database.execSQL("DROP TABLE water")
                database.execSQL("ALTER TABLE water_new RENAME TO water")


                //weight table
                database.execSQL("CREATE TABLE weight_new (id INTEGER NOT NULL, " +
                        "datetime INTEGER NOT NULL, amount REAL NOT NULL, " +
                        "PRIMARY KEY (id))")
                database.execSQL("INSERT INTO weight_new (id, datetime, amount) " +
                        "SELECT _id, datetime, weight FROM weight")
                database.execSQL("DROP TABLE weight")
                database.execSQL("ALTER TABLE weight_new RENAME TO weight")

                database.execSQL("DROP TABLE user")
                database.execSQL("DROP TABLE date")
            }
        }
    }
}