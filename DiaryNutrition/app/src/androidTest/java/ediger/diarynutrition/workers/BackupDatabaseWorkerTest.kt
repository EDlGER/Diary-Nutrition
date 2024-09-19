package ediger.diarynutrition.workers

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker.Result
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import ediger.diarynutrition.BACKUP_NAME
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class BackupDatabaseWorkerTest {
    private lateinit var context: Context
    private lateinit var worker: BackupDatabaseWorker

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        worker = TestListenableWorkerBuilder<BackupDatabaseWorker>(context).build()

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun serializeDatabase_fileCreated() = runBlocking {
        val backupFile = File(context.filesDir, BACKUP_NAME)
        if (backupFile.exists()) assertThat(backupFile.delete(), `is`(true))

        val result = worker.doWork()
        assertThat(result, `is`(Result.success()))

        assertThat(backupFile.exists(), `is`(true))
    }
}