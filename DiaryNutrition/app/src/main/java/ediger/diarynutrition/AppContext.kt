package ediger.diarynutrition

import androidx.core.content.getSystemService
import androidx.multidex.MultiDexApplication
import ediger.diarynutrition.data.DiaryRepository
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.DiaryDatabase.Companion.getInstance
import ediger.diarynutrition.database.DbDiary
import java.util.*

class AppContext : MultiDexApplication() {
    private val mAppExecutors = AppExecutors()

    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(applicationContext)

        //TODO delete
        //sDbDiary = new DbDiary(this);
        //DatabaseCopier.getPreferences(this).execute();
    }

    val database: DiaryDatabase?
        get() = getInstance(this)

    val repository: DiaryRepository
        get() = DiaryRepository.getInstance(database, mAppExecutors)

    companion object {
        //TODO delete
        //Date of chosen day (begin of the day - 00:00) in milliseconds
        // var date: Long = 0
        val dbDiary: DbDiary? = null
        lateinit var instance: AppContext

    }
//
//    init {
//        instance = this
//    }
}