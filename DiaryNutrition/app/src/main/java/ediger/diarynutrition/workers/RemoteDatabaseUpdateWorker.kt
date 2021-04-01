package ediger.diarynutrition.workers

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBeanBuilder
import ediger.diarynutrition.*
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Food
import java.io.File
import java.lang.Exception
import java.util.concurrent.Executors

class RemoteDatabaseUpdateWorker(context: Context, params: WorkerParameters): ListenableWorker(context, params) {

    private val dbLanguage = PreferenceHelper.getValue(KEY_LANGUAGE_DB, String::class.java, "en")

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            val storageRef = Firebase.storage.reference
            val databaseRef = storageRef.child("food/food_$dbLanguage.csv")
            val localFile = File.createTempFile("food_$dbLanguage", "csv")

            val callback = OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                try {
                    val columns = arrayOf("name", "cal", "carbo", "prot", "fat", "verified", "gi")
                    val strat = ColumnPositionMappingStrategy<Food>().apply {
                        type = Food::class.java
                        setColumnMapping(*columns)
                    }
                    val foodList = CsvToBeanBuilder<Food>(localFile.bufferedReader())
                            .withMappingStrategy(strat)
                            .build()
                            .parse()
                    val database = DiaryDatabase.getInstance(applicationContext)
                    val ids = database.foodDao().insertAllFood(foodList)
                    ids.forEachIndexed { i, id ->
                        if (id == -1L) database.foodDao().updateFoodByNameAndCal(
                                foodList[i].name,
                                foodList[i].cal,
                                foodList[i].verified,
                                foodList[i].gi,
                                foodList[i].user
                        )
                    }
                    val version = inputData.getInt(
                            KEY_ONLINE_DB_VERSION,
                            PreferenceHelper.getValue(KEY_LOCAL_DB_VERSION, Integer::class.java, 0)
                    )
                    PreferenceHelper.setValue(KEY_LOCAL_DB_VERSION, version)

                    completer.set(Result.success())
                } catch (e: Exception) {
                    completer.set(Result.failure())
                }
            }

            databaseRef.getFile(localFile).addOnSuccessListener(
                    Executors.newSingleThreadExecutor(),
                    callback
            ).addOnFailureListener {
                completer.set(Result.failure())
            }

            return@getFuture callback
        }
    }

    companion object {
        const val TAG = "RemoteDbUpdateWorker"
    }

}

