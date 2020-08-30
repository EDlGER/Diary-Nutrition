package ediger.diarynutrition.workers

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import ediger.diarynutrition.LiveDataTestUtil
import ediger.diarynutrition.data.source.DiaryDatabase
import ediger.diarynutrition.data.source.entities.Food
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RestoreDatabaseWorkerTest {

    private lateinit var context: Context
    private lateinit var worker: RestoreDatabaseWorker

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        worker = TestListenableWorkerBuilder<RestoreDatabaseWorker>(context).build()
    }

    @Test
    fun testDoWork_success() = runBlocking {
        val result = worker.doWork()
        assertThat(result, `is`(Result.success()))
    }
}