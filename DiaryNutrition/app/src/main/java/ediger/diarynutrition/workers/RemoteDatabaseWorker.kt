package ediger.diarynutrition.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import ediger.diarynutrition.BuildConfig
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.KEY_LOCAL_DB_VERSION
import ediger.diarynutrition.PreferenceHelper
import kotlinx.coroutines.coroutineScope
import java.io.File

class RemoteDatabaseWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    //WiFi constraints
    //One time per day
    
    val localVersion = PreferenceHelper.getValue(KEY_LOCAL_DB_VERSION, Integer::class.java, 1)
    val dbLanguage = PreferenceHelper.getValue(KEY_LANGUAGE_DB, String::class.java, "en")

    override suspend fun doWork(): Result = coroutineScope {
        val storageRef = Firebase.storage.reference
        var versionsRef = storageRef.child("versions.json")
        var databaseRef = storageRef.child("food/food_$dbLanguage.csv")

        val localFile = File.createTempFile("versions", "json")
        versionsRef.getFile(localFile).addOnSuccessListener {
            Log.w(TAG, "File was downloaded")
            val gson = GsonBuilder().create()
            val onlineVersionInfo = gson.fromJson<OnlineVersionInfo>(localFile.bufferedReader(), OnlineVersionInfo::class.java)
            if (BuildConfig.VERSION_NAME == onlineVersionInfo.appVersion &&
                    localVersion < onlineVersionInfo.dbVersion
            ) {
                // It will trigger work chain to download the food_$$.csv
                // TODO: Next work should get access to onlineVersionInfo.dbVersion
                Result.success()
            }
        }.addOnFailureListener {
            Result.failure()
        }
        //Log.w(TAG, downloadTask.snapshot.bytesTransferred.toString())

        Result.failure()
    }

    class OnlineVersionInfo(
            @SerializedName("app_version") var appVersion: String,
            @SerializedName("db_version") var dbVersion: Int
    )

    companion object {
        const val TAG = "RemoteDatabaseWorker"
    }
}