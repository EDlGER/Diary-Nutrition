package ediger.diarynutrition.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.ListenableWorker.Result
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class RemoteDatabaseWorkerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun checkVersionAndUpdate() {
        val worker = TestListenableWorkerBuilder<RemoteDatabaseWorker>(context).build()
        val result = worker.startWork().get(10, TimeUnit.SECONDS)
        assertThat(result, `is`(Result.success()))
    }

}
