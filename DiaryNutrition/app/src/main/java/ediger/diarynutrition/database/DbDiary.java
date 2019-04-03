package ediger.diarynutrition.database;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.SettingsFragment;
import ediger.diarynutrition.objects.AppContext;

public class DbDiary {

    public static String ALIAS_ID = "_id";

    /** Поля таблицы Record */
    public static String ALIAS_FOOD_ID = "food_id";
    public static String ALIAS_MEAL_ID = "meal_id";
    public static String ALIAS_SERVING = "serving";
    public static String ALIAS_RECORD_DATETIME = "record_datetime";

    /** Поля таблицы Food */
    public static String ALIAS_FOOD_NAME = "food_name";
    public static String ALIAS_CAL = "cal";
    public static String ALIAS_CARBO = "carbo";
    public static String ALIAS_PROT = "prot";
    public static String ALIAS_FAT = "fat";
    public static String ALIAS_FAV = "favor";
    public static String ALIAS_USR = "usr";

    /** Поля таблицы Meal */
    public static String ALIAS_M_NAME = "name";

    /** Поля таблицы Date */
    public static String ALIAS_DATETIME = "datetime";

    /** Поля таблицы Weight */
    public static String ALIAS_WEIGHT = "weight";

    /**Поля таблицы Water */
    //ALIAS_DATETIME = "datetime"
    public static String ALIAS_AMOUNT = "amount";

    public static String ALIAS_SUM_AMOUNT = "sum_amount";

    private static final int DB_VERSION = 2;

    //private static final String DB_FOLDER = "/data/data/ediger.diarynutrition/databases/";

    private static final String TABLE_RECORD = "record";
    private static final String TABLE_FOOD = "food";
    private static final String TABLE_MEAL = "meal";
    private static final String TABLE_DATE = "date";
    private static final String TABLE_WEIGHT = "weight";
    private static final String TABLE_WATER = "water";

    private String dbName;

    private DbHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;


    public SQLiteDatabase getDb() {
        return db;
    }

    public DbDiary(Context context) {
        this.context = context;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String dbLanguage = pref.getString(SettingsFragment.KEY_PREF_DATA_LANGUAGE, "");

        if (dbLanguage.isEmpty()) {
            SharedPreferences.Editor editor = pref.edit();
            if (Locale.getDefault().getLanguage().equals("ru")
                    || Locale.getDefault().getLanguage().equals("uk")) {
                dbLanguage = "ru";
                editor.putString(SettingsFragment.KEY_PREF_DATA_LANGUAGE, "ru");
            } else {
                dbLanguage = "en";
                editor.putString(SettingsFragment.KEY_PREF_DATA_LANGUAGE, "en");
            }
            editor.apply();
        }

        if (dbLanguage.equals("ru") || dbLanguage.equals("uk")) {
            dbName = "diary.db";
        } else {
            dbName = "diary_en.db";
        }

        dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
        //BackupDb method won't work without it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            db.disableWriteAheadLogging();
        }
    }

    public String getDbName() {
        return dbName;
    }

    public void backupDb() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String folderPath = "/DiaryNutrition/";
                String currentDbPath = "//data/ediger.diarynutrition/databases/" + dbName;
                String backupDbPath = dbName;

                File folder = new File(sd, folderPath);
                folder.mkdirs();

                File currentDb = new File(data, currentDbPath);
                File backupDb = new File(folder, backupDbPath);

                FileChannel src = new FileInputStream(currentDb).getChannel();
                FileChannel dst = new FileOutputStream(backupDb).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                AppContext.getInstance().recreateDatabase();
                Toast.makeText(context, R.string.message_data_backup, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void restoreDb() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String folderPath = "/DiaryNutrition/";
                String currentDbPath = "//data/ediger.diarynutrition/databases/" + dbName;
                String backupDbPath = dbName;

                File folder = new File(sd, folderPath);
                folder.mkdirs();

                File currentDb = new File(data, currentDbPath);
                File backupDb = new File(folder, backupDbPath);
                FileChannel src = new FileInputStream(backupDb).getChannel();
                FileChannel dst = new FileOutputStream(currentDb).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                AppContext.getInstance().recreateDatabase();
                Toast.makeText(context, R.string.message_data_restore, Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor getMealData(){
        return db.query(TABLE_MEAL, null, null,
                null, null, null, null);
    }

    public Cursor getWaterData(long date) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date + 86356262);
        String sql = "select "
                + "w._id as " + ALIAS_ID
                + ", w.amount as " + ALIAS_AMOUNT
                + ", w.datetime as " + ALIAS_DATETIME
                + " from water w"
                + " where w.datetime between ? and ?"
                + " order by w.datetime asc";
        return db.rawQuery(sql, new String[] {arg1, arg2});
    }

    public Cursor getDayWaterData(long date) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date + 86356262);
        String sql = "select "
                + "sum(w.amount) as " + ALIAS_SUM_AMOUNT
                + " from water w"
                + " where w.datetime between ? and ?";
        return db.rawQuery(sql, new String[] {arg1, arg2});
    }

    public Cursor getDayData(long date) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date + 86356262);  // 86356262 = 24 часа
        String sql = "SELECT "
                + "sum(f.cal/100*r.serving) as " +ALIAS_CAL
                + ",sum(f.[carbo]/100*r.[serving]) as " +ALIAS_CARBO
                + ",sum(f.[prot]/100*r.[serving]) as " +ALIAS_PROT
                + ",sum(f.[fat]/100*r.[serving]) as " +ALIAS_FAT
                + " FROM record r"
                + " inner join food f on r.food_id=f._id"
                + " where r.record_datetime between ? and ?";
        return db.rawQuery(sql, new String[]{arg1, arg2});
    }
    public Cursor getGroupData(long date, int mealId) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
        String arg3 = String.valueOf(mealId);
        String sql = "SELECT "
                + "r.meal_id as " + ALIAS_ID
                + ",sum(f.cal/100*r.serving) as " +ALIAS_CAL
                + ",sum(f.[carbo]/100*r.[serving]) as " +ALIAS_CARBO
                + ",sum(f.[prot]/100*r.[serving]) as " +ALIAS_PROT
                + ",sum(f.[fat]/100*r.[serving]) as " +ALIAS_FAT
                + ",sum(r.serving) as " + ALIAS_SERVING
                + " FROM record r"
                + " inner join food f on r.food_id=f._id"
                + " where (r.record_datetime between ? and ?) and (r.meal_id = ?)";
        return db.rawQuery(sql, new String[]{arg1, arg2, arg3});
    }

    public Cursor getRecordData(long date, long mealID) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
        String arg3 = Long.toString(mealID);
        String sql = "SELECT "
                + "r._id as "+ALIAS_ID
                + ",r.[serving] as "+ALIAS_SERVING
                + ",r.[record_datetime] as "+ALIAS_RECORD_DATETIME
                + ",r.[meal_id] as " +ALIAS_MEAL_ID
                + ",r.[food_id] as " + ALIAS_FOOD_ID
                + ",f.[food_name] as "+ALIAS_FOOD_NAME
                + ",(f.[cal]/100*r.[serving]) as "+ALIAS_CAL
                + ",(f.[carbo]/100*r.[serving]) as "+ALIAS_CARBO
                + ",(f.[prot]/100*r.[serving]) as "+ALIAS_PROT
                + ",(f.[fat]/100*r.[serving]) as "+ALIAS_FAT
                + ",m._id as " + ALIAS_ID
                + ",m.[name] as " +ALIAS_M_NAME
                + " from record r "
                + " inner join food f on r.food_id=f.[_id] "
                + " inner join meal m on r.meal_id=m.[_id] "
                + " where (r.record_datetime between ? and ?) and"
                + " (r.[meal_id] = ?) "
                + " order by r.record_datetime asc";
        return db.rawQuery(sql, new String[]{arg1, arg2, arg3});
    }

    public Cursor getRecordById(long recordId) {
        String arg1 = Long.toString(recordId);
        String sql = "select "
                + "r.serving as " + ALIAS_SERVING
                + ",r.record_datetime as " + ALIAS_RECORD_DATETIME
                + ",r.meal_id as " + ALIAS_MEAL_ID
                + ",r.food_id as " + ALIAS_FOOD_ID
                + " from record r "
                + " where r._id = ?";
        return db.rawQuery(sql, new String[]{arg1});
    }

    public Cursor getAllWeight() {
        String sql = "select "
                + "w._id as " + ALIAS_ID
                + ",w.weight as " + ALIAS_WEIGHT
                + ",w.datetime as " + ALIAS_DATETIME
                + " from weight w "
                + "order by w.datetime desc";
        return db.rawQuery(sql,null);
    }

    public Cursor getWeightFrom(long from) {
        Calendar calendar = Calendar.getInstance();
        long date = calendar.getTimeInMillis();
        String arg1 = Long.toString(date - from);
        String arg2 = Long.toString(date);
        String sql = "select "
                + "w._id as " + ALIAS_ID
                + ",w.weight as " + ALIAS_WEIGHT
                + ",w.datetime as " + ALIAS_DATETIME
                + " from weight w"
                + " where w.datetime between ? and ?"
                + " order by w.datetime asc";
        return db.rawQuery(sql,new String[] {arg1,arg2});
    }

    public Cursor getWeight(long date) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date + 86356262);
        String sql = "select "
                + "w._id as " + ALIAS_ID
                + ",w.weight as " + ALIAS_WEIGHT
                + ",w.datetime as " + ALIAS_DATETIME
                + " from weight w "
                + "where w.datetime between ? and ?";
        return db.rawQuery(sql,new String[] {arg1,arg2});
    }

    public void addWeight(long date, float weight) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_DATETIME,date);
        cv.put(ALIAS_WEIGHT,weight);
        db.insert(TABLE_WEIGHT,null,cv);
    }

    public void setWeight(long date,float weight) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_WEIGHT,weight);
        String where = ALIAS_DATETIME + " = " + date;
        db.update(TABLE_WEIGHT, cv, where, null);
    }

    public void delWeight(long id) {
        db.delete(TABLE_WEIGHT, ALIAS_ID + " = " + id, null);
    }

    public Cursor getDate(){
        return db.query(TABLE_DATE, null, null, null, null, null, null);
    }

    public void setDate(long date) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_DATETIME,date);
        String where = "_id = 1";
        db.update(TABLE_DATE, cv, where, null);
    }

    public Cursor getAllFood() {
        String sql = "select "
                + "f._id as "+ ALIAS_ID
                + ",f.food_name as "+ ALIAS_FOOD_NAME
                + ",f.cal as "+ ALIAS_CAL
                + ",f.carbo as "+ ALIAS_CARBO
                + ",f.prot as "+ ALIAS_PROT
                + ",f.fat as "+ ALIAS_FAT
                + ",f.favor as "+ ALIAS_FAV
                + ",f.usr as "+ ALIAS_USR
                + " from food f "
                + "where f.usr > -1"
                + " order by f.food_name asc";
        return  db.rawQuery(sql, null);
    }

    public Cursor getUserFood() {
        String sql = "select "
                + "f._id as "+ ALIAS_ID
                + ",f.food_name as "+ ALIAS_FOOD_NAME
                + ",f.cal as "+ ALIAS_CAL
                + ",f.carbo as "+ ALIAS_CARBO
                + ",f.prot as "+ ALIAS_PROT
                + ",f.fat as "+ ALIAS_FAT
                + ",f.favor as "+ ALIAS_FAV
                + ",f.usr as "+ ALIAS_USR
                + " from food f "
                + " where f.usr > 0"
                + " order by f.food_name asc";
        return  db.rawQuery(sql,null);
    }

    public Cursor getFavorFood() {
        String sql = "select "
                + "f._id as "+ ALIAS_ID
                + ",f.food_name as "+ ALIAS_FOOD_NAME
                + ",f.cal as "+ ALIAS_CAL
                + ",f.carbo as "+ ALIAS_CARBO
                + ",f.prot as "+ ALIAS_PROT
                + ",f.fat as "+ ALIAS_FAT
                + ",f.favor as "+ ALIAS_FAV
                + ",f.usr as "+ ALIAS_USR
                + " from food f "
                + " where f.favor = 1 AND f.usr > -1"
                + " order by f.food_name asc";
        return  db.rawQuery(sql,null);
    }

    public Cursor getNameFood(long id) {
        return db.query(TABLE_FOOD,new String[]{ALIAS_FOOD_NAME},ALIAS_ID + "=" + id,null,null,null,null);
    }

    public Cursor getFood(long id) {
        String[] selections = {ALIAS_FOOD_NAME, ALIAS_CAL, ALIAS_CARBO, ALIAS_PROT, ALIAS_FAT};
        return db.query(TABLE_FOOD, selections, ALIAS_ID + "=" + id, null, null, null, null);
    }

    public String[] getListFood() {
        String[] listFood = {
                ALIAS_FOOD_NAME,
                ALIAS_CAL,
                ALIAS_CARBO,
                ALIAS_PROT,
                ALIAS_FAT,
                ALIAS_FAV
        };
        return listFood;
    }
    public String[] getFilterFood() {
        String[] listFood = {
                ALIAS_ID,
                ALIAS_FOOD_NAME,
                ALIAS_CAL,
                ALIAS_CARBO,
                ALIAS_PROT,
                ALIAS_FAT,
                ALIAS_FAV
        };
        return listFood;
    }

    public String[] getListRecords() {
        String[] listRecords = {
                ALIAS_FOOD_NAME,
                ALIAS_CAL,
                ALIAS_CARBO,
                ALIAS_PROT,
                ALIAS_FAT,
                ALIAS_RECORD_DATETIME,
                ALIAS_SERVING
        };
        return listRecords;
    }

    public String[] getListMeal() {
        String[] listMeal = {
                ALIAS_M_NAME
        };
        return listMeal;
    }

    public String[] getListWeight() {
        String[] listWeight = {
                ALIAS_DATETIME,
                ALIAS_WEIGHT
        };
        return listWeight;
    }

    /**
     * f.usr = 0 - стандартный продукт
     * f.usr > 0 - пользовательский
     * f.usr = -1  - скрытый пользовательский
     * f.usr = -2 - скрытый стандартный
     * Нужно для определения использования пользовательского продукта записью
     */
    public void addRec(long foodId, int serv, long datetime, int meal) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FOOD_ID, foodId);
        cv.put(ALIAS_SERVING, serv);
        cv.put(ALIAS_RECORD_DATETIME, datetime);
        cv.put(ALIAS_MEAL_ID, meal);
        db.insert(TABLE_RECORD, null, cv);

        cv.clear();
        String sql = "select "
                + "f.[usr] "
                + "from food f "
                + "where f._id = ? ";
        Cursor usr = db.rawQuery(sql, new String[]{"" + foodId});
        usr.moveToFirst();
        if (usr.getInt(0) > 0) {
            cv.put(ALIAS_USR,usr.getInt(0) + 1);
            String where = ALIAS_ID + "=" + foodId;
            db.update(TABLE_FOOD,cv,where,null);
        }
        usr.close();

    }

    public void delRec(long id) {
        String sql = "select "
                + "r.food_id "
                + "from record r "
                + "where r._id = ?";
        Cursor usr = db.rawQuery(sql, new String[]{"" + id});
        usr.moveToFirst();
        String food_id = usr.getString(0);
        sql = "select "
                + "f.usr "
                + "from food f "
                + "where f._id = ?";
        usr = db.rawQuery(sql,new String[]{food_id});
        usr.moveToFirst();
        if (usr.getInt(0) > 0) {
            ContentValues cv = new ContentValues();
            cv.put(ALIAS_USR, usr.getInt(0) - 1);
            String where = ALIAS_ID + "=" + food_id;
            db.update(TABLE_FOOD, cv, where, null);
        }
        db.delete(TABLE_RECORD, ALIAS_ID + " = " + id, null);
        usr.close();
    }

    public void editRec(long recordId, long foodId, int serving,long datetime, int mealId) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FOOD_ID, foodId);
        cv.put(ALIAS_SERVING, serving);
        cv.put(ALIAS_RECORD_DATETIME, datetime);
        cv.put(ALIAS_MEAL_ID, mealId);
        String where = ALIAS_ID + "=" + recordId;
        db.update(TABLE_RECORD, cv, where, null);
    }

    public void addFood(String name, float cal, float carbo,
                        float prot, float fat) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FOOD_NAME,name);
        cv.put(ALIAS_CAL, cal);
        cv.put(ALIAS_CARBO,carbo);
        cv.put(ALIAS_PROT, prot);
        cv.put(ALIAS_FAT, fat);
        cv.put(ALIAS_USR, 1);
        db.insert(TABLE_FOOD, null, cv);
    }


    public void delFood(long id) {
        String sql = "select "
                + "f.[usr] "
                + "from food f "
                + "where f._id = ? ";
        Cursor usr = db.rawQuery(sql,new String[] {"" + id});
        usr.moveToFirst();
        if (usr.getInt(0) > 1) {
            ContentValues cv = new ContentValues();
            cv.put(ALIAS_USR, -1);
            String where = ALIAS_ID + "=" + id;
            db.update(TABLE_FOOD, cv, where, null);
        } else if (usr.getInt(0) == 0) {
            ContentValues cv = new ContentValues();
            cv.put(ALIAS_USR, -2);
            String where = ALIAS_ID + "=" + id;
            db.update(TABLE_FOOD, cv, where, null);
        } else {
            db.delete(TABLE_FOOD, ALIAS_ID + " = " + id,null);
        }
        usr.close();
    }

    public void editFood(long id, String name, float cal, float carbo,
                         float prot, float fat) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FOOD_NAME,name);
        cv.put(ALIAS_CAL,cal);
        cv.put(ALIAS_CARBO,carbo);
        cv.put(ALIAS_PROT,prot);
        cv.put(ALIAS_FAT, fat);
        String where = ALIAS_ID + " = " + id;
        db.update(TABLE_FOOD, cv, where, null);

    }

    public int getFavor(long id) {
        int res = 0;

        String sql = "select "
                + "f.favor "
                + "from food f "
                + "where r._id = ?";
        Cursor fav = db.rawQuery(sql, new String[]{"" + id});
        fav.moveToFirst();
        res = fav.getInt(0);
        fav.close();

        return res;
    }

    public void setFavor(long id, int state) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FAV,state);
        String where = ALIAS_ID + " = " + id;
        db.update(TABLE_FOOD,cv,where,null);
    }

    public void addWater(long date, int amount) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_DATETIME, date);
        cv.put(ALIAS_AMOUNT, amount);
        db.insert(TABLE_WATER, null, cv);
    }

    public void delWater(long id) {
        db.delete(TABLE_WATER, ALIAS_ID + " = " + id, null);
    }

    public void editWater(long id, long datetime, int amount) {
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_DATETIME, datetime);
        cv.put(ALIAS_AMOUNT, amount);
        db.update(TABLE_WATER, cv, ALIAS_ID + " = " + id, null);
    }

    private class DbHelper extends SQLiteAssetHelper {

        private final Context mContext;

        public DbHelper(Context context) {
            super(context, dbName, null, DB_VERSION);
            mContext = context;
            //setForcedUpgrade();
        }
    }
}
