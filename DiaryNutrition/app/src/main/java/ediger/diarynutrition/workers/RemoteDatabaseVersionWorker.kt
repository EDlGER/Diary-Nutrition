package ediger.diarynutrition.workers

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import ediger.diarynutrition.BuildConfig
import ediger.diarynutrition.KEY_LOCAL_DB_VERSION
import ediger.diarynutrition.KEY_ONLINE_DB_VERSION
import ediger.diarynutrition.PreferenceHelper
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RemoteDatabaseVersionWorker(appContext: Context, params: WorkerParameters) : ListenableWorker(appContext, params) {
    // WiFi constraints
    // One time per day -> Make it Periodic
    private val isTesting = inputData.getInt(KEY_LOCAL_DB_VERSION, -1) != -1

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var updateRequest: OneTimeWorkRequest

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            val storageRef = Firebase.storage.reference
            val versionsRef = storageRef.child("versions.json")
            val localFile = File.createTempFile("versions", "json")
            val localVersion = PreferenceHelper.getValue(KEY_LOCAL_DB_VERSION, Integer::class.java, 1)

            val callback = OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                try {
                    Log.w(TAG, "File was downloaded")
                    val onlineVersionInfo = GsonBuilder().create().fromJson<OnlineVersionInfo>(
                            localFile.bufferedReader(),
                            OnlineVersionInfo::class.java
                    )
                    if ((BuildConfig.VERSION_NAME == onlineVersionInfo.appVersion &&
                            localVersion < onlineVersionInfo.dbVersion)
                            || isTesting
                    ) {
                        updateRequest = OneTimeWorkRequestBuilder<RemoteDatabaseUpdateWorker>()
                                .setInputData(workDataOf(KEY_ONLINE_DB_VERSION to onlineVersionInfo.dbVersion))
                                .build()
                        WorkManager.getInstance(applicationContext)
                                .enqueueUniqueWork(
                                        RemoteDatabaseUpdateWorker.TAG,
                                        ExistingWorkPolicy.KEEP,
                                        updateRequest
                                )
                        completer.set(Result.success())
                    } else {
                        completer.set(Result.failure())
                    }
                } catch (e: Exception) {
                    completer.set(Result.failure())
                }
            }

            versionsRef.getFile(localFile).addOnSuccessListener(
                    Executors.newSingleThreadExecutor(),
                    callback
            ).addOnFailureListener {
                completer.set(Result.failure())
            }
            return@getFuture callback
        }


    }

    class OnlineVersionInfo(
            @SerializedName("app_version") var appVersion: String,
            @SerializedName("db_version") var dbVersion: Int
    )

    companion object {
        const val TAG = "RemoteDbVersionWorker"
    }
}