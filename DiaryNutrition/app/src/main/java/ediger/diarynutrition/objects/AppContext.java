package ediger.diarynutrition.objects;

import android.app.Application;

import ediger.diarynutrition.data.DatabaseCopier;
import ediger.diarynutrition.database.DbDiary;

public class AppContext extends Application{

    /* Date of chosen day (begin of the day - 00:00) in milliseconds */
    private static long sDate;

    private static DbDiary sDbDiary;
    private static AppContext sInstance;

    public AppContext() {sInstance = this;}

    @Override
    public void onCreate() {
        super.onCreate();

        sDbDiary = new DbDiary(this);

        //DatabaseCopier.getInstance(this).execute();

    }

    public static AppContext getInstance() {
        return sInstance;
    }

    public static DbDiary getDbDiary() {
            return sDbDiary;
    }

    public static long getDate() {
        return sDate;
    }

    public static void setDate(long date) {
        sDate = date;
    }

}
