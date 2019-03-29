package ediger.diarynutrition.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import ediger.diarynutrition.data.dao.FoodDao;
import ediger.diarynutrition.data.dao.MealDao;
import ediger.diarynutrition.data.dao.RecordDao;
import ediger.diarynutrition.data.dao.SummaryDao;
import ediger.diarynutrition.data.dao.WaterDao;
import ediger.diarynutrition.data.dao.WeightDao;

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
            database.execSQL("CREATE TABLE `record`(" +
                    "`_id` INTEGER, `mealId` INTEGER, `foodId` INTEGER, `serving` INTEGER, " +
                    "`datetime` BIGINT, PRIMARY KEY(`_id`))");

            database.execSQL("CREATE TABLE `food`(" +
                    "`_id` INTEGER, `food_name` TEXT, `cal` REAL, `prot` REAL, " +
                    "`fat` REAL, `carbo` REAL, `favor` INTEGER, `usr` INTEGER, PRIMARY KEY(`_id`))");

            database.execSQL("CREATE TABLE `meal`(`_id` INTEGER, `name` TEXT, PRIMARY KEY(`_id`))");

            database.execSQL("CREATE TABLE `weight`(`_id` INTEGER, `weight` REAL, " +
                    "`datetime` BIGINT, PRIMARY KEY(`_id`))");

            database.execSQL("CREATE TABLE `water`(`_id` INTEGER, `amount` INTEGER, " +
                    "`datetime` BIGINT, PRIMARY KEY(`_id`))");

        }
    };

























}
