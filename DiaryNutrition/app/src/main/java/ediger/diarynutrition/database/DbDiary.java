package ediger.diarynutrition.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ediger.diarynutrition.objects.AppContext;


/**
 * Created by root on 14.05.15.
 */
public class DbDiary {

    private static final String LOG_TAG = DbDiary.class.getName();
    private static final String DB_NAME = "diary.db";
    private static final int DB_VERSION = 1;
    private static final String DB_FOLDER = "/data/data/ediger.diarynutrition/databases/";
    private static final String DB_PATH = DB_FOLDER + DB_NAME;
    private static final int DB_FILES_COPY_BUFFER_SIZE = 1024;

    private static final String TABLE_RECORD = "record";
    private static final String TABLE_FOOD = "food";
    private static final String TABLE_MEAL = "meal";
    private static final String TABLE_DATE = "date";
    private static final String TABLE_WEIGHT = "weight";

    private DbHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;


    public SQLiteDatabase getDb() {
        return db;
    }

    //Поля таблицы Record
    public static String ALIAS_ID="_id";
    public static String ALIAS_FOOD_ID="food_id";
    public static String ALIAS_MEAL_ID="meal_id";
    public static String ALIAS_SERVING="serving";
    public static String ALIAS_RECORD_DATETIME="record_datetime";

    //Поля таблицы Food
    public static String ALIAS_ID_FOOD = "_id";
    public static String ALIAS_FOOD_NAME="food_name";
    public static String ALIAS_CAL="cal";
    public static String ALIAS_CARBO="carbo";
    public static String ALIAS_PROT="prot";
    public static String ALIAS_FAT="fat";
    public static String ALIAS_FAV="favor";


    public static String ALIAS_F_NAME="food_name";
    public static String ALIAS_F_CAL="cal";
    public static String ALIAS_F_CARBO="carbo";
    public static String ALIAS_F_PROT="prot";
    public static String ALIAS_F_FAT="fat";
    public static String ALIAS_F_FAV="favor";
    public static String ALIAS_F_USR="usr";

    //Поля таблицы Meal
    public static String ALIAS_M_ID = "_id";
    public static String ALIAS_M_NAME = "name";

    //Поля таблицы Date
    public static String ALIAS_DATETIME = "datetime";

    //Поля таблицы Weight
    public static String ALIAS_WEIGHT = "weight";

    public DbDiary(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    //Запрос в БД
    public Cursor getAllRecords(){
        String sql = "select "
                + "r._id as "+ALIAS_ID
                + ",r.[serving] as "+ALIAS_SERVING
                + ",r.[record_datetime] as "+ALIAS_RECORD_DATETIME
                + ",f.[food_name] as "+ALIAS_FOOD_NAME
                + ",(f.[cal]/100*r.[serving]) as "+ALIAS_CAL
                + ",(f.[carbo]/100*r.[serving]) as "+ALIAS_CARBO
                + ",(f.[prot]/100*r.[serving]) as "+ALIAS_PROT
                + ",(f.[fat]/100*r.[serving]) as "+ALIAS_FAT
                + " from record r "
                + " inner join food f on r.food_id=f.[_id] ";
        return db.rawQuery(sql,null);
    }

    public Cursor getMealData(){
        return db.query(TABLE_MEAL, null, null, null, null, null, null);
    }

    public Cursor getDayData(long date) {
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
        String sql = "select "
                + "sum(f.cal/100*r.serving) as " +ALIAS_CAL
                + ",sum(f.[carbo]/100*r.[serving]) as " +ALIAS_CARBO
                + ",sum(f.[prot]/100*r.[serving]) as " +ALIAS_PROT
                + ",sum(f.[fat]/100*r.[serving]) as " +ALIAS_FAT
                + " FROM record r"
                + " inner join food f on r.food_id=f._id"
                + " where r.record_datetime between ? and ?";
        return db.rawQuery(sql, new String[]{arg1, arg2});
    }
    public Cursor getGroupData(long date){
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
        String sql = "SELECT "
                + "r.meal_id as " +ALIAS_M_ID
                + ",sum(f.cal/100*r.serving) as " +ALIAS_CAL
                + ",sum(f.[carbo]/100*r.[serving]) as " +ALIAS_CARBO
                + ",sum(f.[prot]/100*r.[serving]) as " +ALIAS_PROT
                + ",sum(f.[fat]/100*r.[serving]) as " +ALIAS_FAT
                + " FROM record r"
                + " inner join food f on r.food_id=f._id"
                + " where r.record_datetime between ? and ?"
                + "group by r.meal_id";
        return db.rawQuery(sql, new String[]{arg1, arg2});
    }

    public Cursor getRecordData(long date, long mealID){
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
        String arg3 = Long.toString(mealID);
        String sql = "select "
                + "r._id as "+ALIAS_ID
                + ",r.[serving] as "+ALIAS_SERVING
                + ",r.[record_datetime] as "+ALIAS_RECORD_DATETIME
                + ",r.[meal_id] as " +ALIAS_MEAL_ID
                + ",f.[food_name] as "+ALIAS_FOOD_NAME
                + ",(f.[cal]/100*r.[serving]) as "+ALIAS_CAL
                + ",(f.[carbo]/100*r.[serving]) as "+ALIAS_CARBO
                + ",(f.[prot]/100*r.[serving]) as "+ALIAS_PROT
                + ",(f.[fat]/100*r.[serving]) as "+ALIAS_FAT
                + ",m._id as " +ALIAS_M_ID
                + ",m.[name] as " +ALIAS_M_NAME
                + " from record r "
                + " inner join food f on r.food_id=f.[_id] "
                + " inner join meal m on r.meal_id=m.[_id] "
                + " where (r.record_datetime between ? and ?) and"
                + " (r.[meal_id] = ?) "
                + " order by r.record_datetime asc";
        return db.rawQuery(sql, new String[]{arg1, arg2, arg3});
    }

    public Cursor getAllWeight() {
        String sql = "select "
                + "w._id as " + ALIAS_ID
                + ",w.weight as " + ALIAS_WEIGHT
                + ",w.datetime as " + ALIAS_DATETIME
                + " from weight w "
                + "order by w.datetime dec";
        return db.rawQuery(sql,null);
    }

    public Cursor getWeight(long date){
        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
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

    public Cursor getDate(){
        return db.query(TABLE_DATE, null, null, null, null, null, null);
    }

    public void editDate(long date){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_DATETIME,date);
        String where = "_id = 1";
        db.update(TABLE_DATE, cv, where, null);
    }

    public Cursor getAllFood(){
        String sql = "select "
                + "f._id as "+ALIAS_ID_FOOD
                + ",f.[food_name] as "+ALIAS_F_NAME
                + ",f.[cal] as "+ALIAS_F_CAL
                + ",f.[carbo] as "+ALIAS_F_CARBO
                + ",f.[prot] as "+ALIAS_F_PROT
                + ",f.[fat] as "+ALIAS_F_FAT
                + ",f.[favor] as "+ALIAS_F_FAV
                + ",f.[usr] as "+ALIAS_F_USR
                + " from food f "
                + "where f.[usr] > -1";
        return  db.rawQuery(sql, null);
    }

    public Cursor getUserFood(){
        String sql = "select "
                + "f._id as "+ALIAS_ID_FOOD
                + ",f.[food_name] as "+ALIAS_F_NAME
                + ",f.[cal] as "+ALIAS_F_CAL
                + ",f.[carbo] as "+ALIAS_F_CARBO
                + ",f.[prot] as "+ALIAS_F_PROT
                + ",f.[fat] as "+ALIAS_F_FAT
                + ",f.[favor] as "+ALIAS_F_FAV
                + ",f.[usr] as "+ALIAS_F_USR
                + " from food f "
                + " where f.[usr] > 0"
                + " order by f.food_name asc";
        return  db.rawQuery(sql,null);
    }

    public Cursor getFavorFood(){
        String sql = "select "
                + "f._id as "+ALIAS_ID_FOOD
                + ",f.[food_name] as "+ALIAS_F_NAME
                + ",f.[cal] as "+ALIAS_F_CAL
                + ",f.[carbo] as "+ALIAS_F_CARBO
                + ",f.[prot] as "+ALIAS_F_PROT
                + ",f.[fat] as "+ALIAS_F_FAT
                + ",f.[favor] as "+ALIAS_F_FAV
                + ",f.[usr] as "+ALIAS_F_USR
                + " from food f "
                + " where f.[favor] = 1"
                + " order by f.food_name asc";
        return  db.rawQuery(sql,null);
    }

    public String[] getListFood() {
        String[] listFood = {
                ALIAS_F_NAME,
                ALIAS_F_CAL,
                ALIAS_F_CARBO,
                ALIAS_F_PROT,
                ALIAS_FAT,
                ALIAS_F_FAV
        };
        return listFood;
    }
    public String[] getFilterFood() {
        String[] listFood = {
                ALIAS_ID_FOOD,
                ALIAS_F_NAME,
                ALIAS_F_CAL,
                ALIAS_F_CARBO,
                ALIAS_F_PROT,
                ALIAS_FAT,
                ALIAS_F_FAV
        };
        return listFood;
    }

    public String[] getListRecords(){
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

    public String[] getListMeal(){
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


    public void addRec(long id, int serv, long datetime, int meal){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FOOD_ID, id);
        cv.put(ALIAS_SERVING, serv);
        cv.put(ALIAS_RECORD_DATETIME, datetime);
        cv.put(ALIAS_MEAL_ID, meal);
        db.insert(TABLE_RECORD, null, cv);

        cv.clear();
        String sql = "select "
                + "f.[usr] "
                + "from food f "
                + "where f._id = ? ";
        Cursor usr = db.rawQuery(sql, new String[]{"" + id});
        usr.moveToFirst();
        if (usr.getInt(0) > 0){
            cv.put(ALIAS_F_USR,usr.getInt(0) + 1);
            String where = ALIAS_ID_FOOD + "=" + id;
            db.update(TABLE_FOOD,cv,where,null);
        }
        usr.close();
        //Нужно для определения использования пользовательского продукта записью
        //f.usr = 0 - стандартный продукт
        //f.usr > 0 - пользовательский
        //f.usr = -1  - скрытый пользовательский
    }

    public void delRec(long id){
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
        if (usr.getInt(0) > 0){
            ContentValues cv = new ContentValues();
            cv.put(ALIAS_F_USR, usr.getInt(0) - 1);
            String where = ALIAS_ID_FOOD + "=" + usr.getString(0);;
            db.update(TABLE_FOOD,cv,where,null);
        }
        db.delete(TABLE_RECORD, ALIAS_ID + " = " + id, null);
        usr.close();
    }


    public void addFood(String name, float cal, float carbo,
                        float prot, float fat){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_F_NAME,name);
        cv.put(ALIAS_F_CAL, cal);
        cv.put(ALIAS_F_CARBO,carbo);
        cv.put(ALIAS_F_PROT, prot);
        cv.put(ALIAS_F_FAT, fat);
        cv.put(ALIAS_F_USR, 1);
        db.insert(TABLE_FOOD, null, cv);
    }


    public void delFood(long id){
        String sql = "select "
                + "f.[usr] "
                + "from food f "
                + "where f._id = ? ";
        Cursor usr = db.rawQuery(sql,new String[]{""+id});
        usr.moveToFirst();
        if (usr.getInt(0) > 1){
            ContentValues cv = new ContentValues();
            cv.put(ALIAS_F_USR, -1);
            String where = ALIAS_ID_FOOD + "=" + id;
            db.update(TABLE_FOOD, cv, where, null);
        }
        else {
            db.delete(TABLE_FOOD,ALIAS_ID_FOOD + " = " + id,null);
        }
        usr.close();
    }

    public void editFood(long id, String name, float cal, float carbo,
                         float prot, float fat){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_F_NAME,name);
        cv.put(ALIAS_F_CAL,cal);
        cv.put(ALIAS_F_CARBO,carbo);
        cv.put(ALIAS_F_PROT,prot);
        cv.put(ALIAS_F_FAT, fat);
        String where = ALIAS_ID_FOOD + " = " + id;
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
        String where = ALIAS_ID_FOOD + " = " + id;
        db.update(TABLE_FOOD,cv,where,null);
    }

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            initialize();
        }

        public void initialize(){
            if (isInitilize() == false) copyDb();
        }

        private boolean isInitilize(){
            SQLiteDatabase checkDb = null;
            Boolean correctVersion = false;
            try{
                checkDb = SQLiteDatabase.openDatabase(DB_PATH,null,
                        SQLiteDatabase.OPEN_READONLY);
                correctVersion = checkDb.getVersion() == DB_VERSION;
            } catch (SQLiteException e){
                Log.w(LOG_TAG, e.getMessage());
            } finally {
                if (checkDb != null) checkDb.close();
            }
            return  checkDb != null && correctVersion;
        }

        private  void copyDb() {

            Context appContext = AppContext.getInstance().getApplicationContext();
            InputStream inStream = null;
            OutputStream outStream = null;

            try {
                inStream = new BufferedInputStream(appContext.getAssets().open(DB_NAME),
                        DB_FILES_COPY_BUFFER_SIZE);
                File dbDir = new File(DB_FOLDER);
                if (!dbDir.exists()) {
                    dbDir.mkdir();
                }
                outStream = new BufferedOutputStream(new FileOutputStream(DB_PATH),
                        DB_FILES_COPY_BUFFER_SIZE);

                byte[] buffer = new byte[DB_FILES_COPY_BUFFER_SIZE];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                outStream.flush();
                outStream.close();
                inStream.close();

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            throw  new SQLiteException(
                    "Call DbDiary.Initialize first. This method should never be called.");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            throw  new SQLiteException(
                    "Call DbDiary.Initialize first. This method should never be called.");
        }
    }
}
