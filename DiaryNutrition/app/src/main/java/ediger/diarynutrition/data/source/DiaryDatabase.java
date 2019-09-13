package ediger.diarynutrition.data.source;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ediger.diarynutrition.data.source.entities.Food;
import ediger.diarynutrition.data.source.entities.Meal;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.data.source.dao.FoodDao;
import ediger.diarynutrition.data.source.dao.MealDao;
import ediger.diarynutrition.data.source.dao.RecordDao;
import ediger.diarynutrition.data.source.dao.SummaryDao;
import ediger.diarynutrition.data.source.dao.WaterDao;
import ediger.diarynutrition.data.source.dao.WeightDao;

@Database(entities = {Record.class, Food.class, Meal.class, Water.class, Weight.class},
    version = 2)
public abstract class DiaryDatabase extends RoomDatabase {

    private static DiaryDatabase sInstance;

    public abstract FoodDao foodDao();
    public abstract MealDao mealDao();
    public abstract RecordDao recordDao();
    public abstract WaterDao waterDao();
    public abstract WeightDao weightDao();
    public abstract SummaryDao summaryDao();

    public static DiaryDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DiaryDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context);
                    //Update database
                }
            }
        }
        return sInstance;
    }

    private static DiaryDatabase buildDatabase(final Context appContext) {

        DatabaseCopier.getInstance(appContext).execute();

        return Room.databaseBuilder(appContext, DiaryDatabase.class,
                    DatabaseCopier.getInstance(appContext).getDatabaseName())
                .addMigrations(MIGRATION_1_2)
                .build();

    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //food table
            database.execSQL("CREATE TABLE food_new (id INTEGER NOT NULL," +
                    "food_name TEXT, " +
                    "cal REAL NOT NULL, carbo REAL NOT NULL, " +
                    "prot REAL NOT NULL, fat REAL NOT NULL, " +
                    "favorite INTEGER DEFAULT 0 NOT NULL, " +
                    "user INTEGER DEFAULT 0 NOT NULL, PRIMARY KEY (id))");
            database.execSQL("INSERT INTO food_new (id, food_name, cal, carbo, prot, fat," +
                    "favorite, user) SELECT _id, food_name, cal, carbo, prot, fat, " +
                    "favor, usr FROM food");
            database.execSQL("DROP TABLE food");
            database.execSQL("ALTER TABLE food_new RENAME TO food");


            //meal table
            database.execSQL("CREATE TABLE meal_new (id INTEGER NOT NULL, " +
                    "name TEXT, PRIMARY KEY (id))");
            database.execSQL("INSERT INTO meal_new (id, name) " +
                    "SELECT _id, name FROM meal");
            database.execSQL("DROP TABLE meal");
            database.execSQL("ALTER TABLE meal_new RENAME TO meal");


            //TODO Foreign keys?
            //record table
            database.execSQL("CREATE TABLE record_new (id INTEGER NOT NULL, " +
                    "food_id INTEGER NOT NULL," +
                    "meal_id INTEGER NOT NULL, " +
                    "record_datetime INTEGER NOT NULL, " +
                    "serving INTEGER NOT NULL," +
                    "PRIMARY KEY (id))");
            database.execSQL("INSERT INTO record_new (id, food_id, meal_id, record_datetime, " +
                    "serving) SELECT _id, food_id, meal_id, record_datetime, serving FROM record");
            database.execSQL("DROP TABLE record");
            database.execSQL("ALTER TABLE record_new RENAME TO record");


            //water table
            database.execSQL("CREATE TABLE IF NOT EXISTS water (_id INTEGER, " +
                    "datetime INTEGER, amount INTEGER, PRIMARY KEY(_id))");
            database.execSQL("CREATE TABLE water_new (id INTEGER NOT NULL, " +
                    "datetime INTEGER NOT NULL, amount INTEGER NOT NULL, PRIMARY KEY (id))");
            database.execSQL("INSERT INTO water_new (id, datetime, amount) " +
                    "SELECT _id, datetime, amount FROM water");
            database.execSQL("DROP TABLE water");
            database.execSQL("ALTER TABLE water_new RENAME TO water");


            //weight table
            database.execSQL("CREATE TABLE weight_new (id INTEGER NOT NULL, " +
                    "datetime INTEGER NOT NULL, weight REAL NOT NULL, " +
                    "PRIMARY KEY (id))");
            database.execSQL("INSERT INTO weight_new (id, datetime, weight) " +
                    "SELECT _id, datetime, weight FROM weight");
            database.execSQL("DROP TABLE weight");
            database.execSQL("ALTER TABLE weight_new RENAME TO weight");



        }
    };



























}
