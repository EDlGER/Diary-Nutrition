package ediger.diarynutrition.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import ediger.diarynutrition.BACKUP_NAME
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
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
    }

    @Test
    fun serializeDatabase_fileCreated() {
        val result = worker.startWork().get()
        assertThat(result, `is`(Result.success()))

        val backupFile = File(context.filesDir, BACKUP_NAME)
        assertThat(backupFile.exists(), `is`(true))
    }

}