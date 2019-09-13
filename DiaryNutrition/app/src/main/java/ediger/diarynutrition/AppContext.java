package ediger.diarynutrition;

import androidx.multidex.MultiDexApplication;

import ediger.diarynutrition.data.DiaryRepository;
import ediger.diarynutrition.data.source.DiaryDatabase;
import ediger.diarynutrition.database.DbDiary;

public class AppContext extends MultiDexApplication {

    //TODO delete
    //Date of chosen day (begin of the day - 00:00) in milliseconds
    private static long sDate;
    private static DbDiary sDbDiary;

    private static AppContext sInstance;

    private AppExecutors mAppExecutors;

    public AppContext() {sInstance = this;}

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceHelper.init(getApplicationContext());
        mAppExecutors = new AppExecutors();

        //TODO delete
        //sDbDiary = new DbDiary(this);
        //DatabaseCopier.getPreferences(this).execute();
    }

    public static AppContext getInstance() {
        return sInstance;
    }

    public static DbDiary getDbDiary() {
            return sDbDiary;
    }

    //TODO delete
    public static long getDate() {
        return sDate;
    }

    //TODO delete
    public static void setDate(long date) {
        sDate = date;
    }

    public DiaryDatabase getDatabase() {
        return DiaryDatabase.getInstance(this);
    }

    public DiaryRepository getRepository() {
        return DiaryRepository.getInstance(getDatabase(), mAppExecutors);
    }

}
