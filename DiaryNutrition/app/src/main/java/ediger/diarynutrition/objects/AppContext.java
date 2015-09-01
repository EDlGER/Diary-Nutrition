package ediger.diarynutrition.objects;

import android.app.Application;

import java.util.Calendar;

import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 17.05.15.
 */
public class AppContext extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        dbDiary = new DbDiary(this);

    }

    private static DbDiary dbDiary;


    public static DbDiary getDbDiary(){
            return dbDiary;
        }

}
