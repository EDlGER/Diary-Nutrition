package ediger.diarynutrition.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import ediger.diarynutrition.KEY_ONLINE_DB_VERSION
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RemoteDatabaseUpdateWorkerTest {

    lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun downloadFoodAndUpdateDatabase() {
        val worker = TestListenableWorkerBuilder<RemoteDatabaseUpdateWorker>(
                context,
                workDataOf(KEY_ONLINE_DB_VERSION to 1)
        ).build()
        val result = worker.startWork().get()

        assertThat(result, `is`(Result.success()))
    }
}