package ediger.diarynutrition.workers

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.ListenableWorker.Result
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import ediger.diarynutrition.KEY_LOCAL_DB_VERSION
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class RemoteDatabaseVersionWorkerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        workManager = WorkManager.getInstance(context)

        val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun checkVersionAndUpdate() {
        val worker = TestListenableWorkerBuilder<RemoteDatabaseVersionWorker>(context)
                .setInputData(workDataOf(KEY_LOCAL_DB_VERSION to 0))
                .build()
        val result = worker.startWork().get()
        assertThat(result, `is`(Result.success()))
    }

    /*@Test
    fun testPeriodicWork() {
        val request = PeriodicWorkRequestBuilder<RemoteDatabaseVersionWorker>(1, TimeUnit.HOURS)
                .setInputData(workDataOf(KEY_LOCAL_DB_VERSION to 0))
                .build()
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        workManager.enqueue(request).result.get()

        var workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))

        testDriver?.setPeriodDelayMet(request.id)

        workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))

    }*/
}
