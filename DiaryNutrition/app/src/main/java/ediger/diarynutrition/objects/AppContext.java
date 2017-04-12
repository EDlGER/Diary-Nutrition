package ediger.diarynutrition.objects;

import android.app.Application;

import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 17.05.15.
 */
public class AppContext extends Application{

    private static DbDiary dbDiary;
    private static AppContext instance;

    public AppContext() {instance = this;}

    @Override
    public void onCreate() {
        super.onCreate();

        dbDiary = new DbDiary(this);

    }

    public static AppContext getInstance(){return instance;}

    public static DbDiary getDbDiary(){
            return dbDiary;
        }

}
