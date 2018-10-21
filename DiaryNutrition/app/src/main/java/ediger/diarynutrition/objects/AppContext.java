package ediger.diarynutrition.objects;

import android.app.Application;

import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 17.05.15.
 */
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
