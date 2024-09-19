package ediger.diarynutrition.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ImportFoodValuesWorkTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun testImportFoodValuesWork() {
        val worker = TestListenableWorkerBuilder<FoodDatabaseWorker>(context).build()

        val result = worker.startWork().get()

        assertThat(result, `is`(Result.success()))
    }
}