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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.objects.AppContext;
import ediger.diarynutrition.objects.Record;


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

    private DbHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;


    public SQLiteDatabase getDb() {
        return db;
    }

    //private ArrayList<Record> listRecords = new ArrayList<Record>();

    //Поля таблицы Record
    public static String ALIAS_ID="_id";
    public static String ALIAS_FOOD_ID="food_id";
    public static String ALIAS_SERVING="serving";
    public static String ALIAS_RECORD_DATETIME="record_datetime";

    //Поля таблицы Food
    public static String ALIAS_ID_FOOD = "_id";
    public static String ALIAS_FOOD_NAME="food_name";
    public static String ALIAS_CAL="cal";
    public static String ALIAS_CARBO="carbo";
    public static String ALIAS_PROT="prot";
    public static String ALIAS_FAT="fat";
    //public static String ALIAS_FAV="favor";


    public static String ALIAS_F_NAME="food_name";
    public static String ALIAS_F_CAL="cal";
    public static String ALIAS_F_CARBO="carbo";
    public static String ALIAS_F_PROT="prot";
    public static String ALIAS_F_FAT="fat";
    public static String ALIAS_F_FAV="favor";
    public static String ALIAS_F_USR="usr";

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

    public Cursor getRecords(long date){

        String arg1 = Long.toString(date);
        String arg2 = Long.toString(date+86356262);
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
                + " inner join food f on r.food_id=f.[_id] "
                + " where r.record_datetime between ? and ? "
                + " order by r.record_datetime asc";
        return db.rawQuery(sql,new String[]{arg1,arg2});
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
                + " from food f ";
        return  db.rawQuery(sql,null);
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
                + " where f.[usr]=1 ";
        return  db.rawQuery(sql,null);
    }

    public String[] getListFood() {
        String[] listFood = {
                ALIAS_F_NAME,
                ALIAS_F_CAL,
                ALIAS_F_CARBO,
                ALIAS_F_PROT,
                ALIAS_FAT
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
                ALIAS_FAT
        };
        return listFood;
    }


    public void addRec(long id, int serv, long datetime){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_FOOD_ID,id);
        cv.put(ALIAS_SERVING,serv);
        cv.put(ALIAS_RECORD_DATETIME,datetime);
        db.insert(TABLE_RECORD, null, cv);
    }

    public void addFood(String name, float cal, float carbo,
                        float prot, float fat){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_F_NAME,name);
        cv.put(ALIAS_F_CAL,cal);
        cv.put(ALIAS_F_CARBO,carbo);
        cv.put(ALIAS_F_PROT,prot);
        cv.put(ALIAS_F_FAT,fat);
        cv.put(ALIAS_F_USR,1);
        db.insert(TABLE_FOOD, null, cv);
    }

    public void delRec(long id){ db.delete(TABLE_RECORD, ALIAS_ID + " = " + id, null);}

    public void delFood(long id){ db.delete(TABLE_FOOD,ALIAS_ID_FOOD +" = "+ id,null);}

    public void editFood(long id, String name, float cal, float carbo,
                         float prot, float fat){
        ContentValues cv = new ContentValues();
        cv.put(ALIAS_F_NAME,name);
        cv.put(ALIAS_F_CAL,cal);
        cv.put(ALIAS_F_CARBO,carbo);
        cv.put(ALIAS_F_PROT,prot);
        cv.put(ALIAS_F_FAT,fat);
        String where = ALIAS_ID_FOOD + " = " + id;
        db.update(TABLE_FOOD, cv, where, null);

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
                if (dbDir.exists() == false) dbDir.mkdir();
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
