package com.mukhtari.app.data.export

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.db.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileWriter

@RunWith(AndroidJUnit4::class)
class ImportExportTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: ImportExportRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        repository = ImportExportRepositoryImpl(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testImportCsvFlow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "test.csv")
        FileWriter(file).use {
            it.write("FullName,FatherName\n")
            it.write("Ali,Ahmed\n")
            it.write("Omer,Osman\n")
        }

        val sessionId = "session1"
        val staging = repository.parseExcelToStaging(file, sessionId)
        assertEquals(2, staging.size)

        repository.validateStagingData(sessionId)

        val validRecords = db.importStagingDao().getStagingBySessionId(sessionId)
        assertEquals(2, validRecords.filter { it.validationStatus == "valid" }.size)

        repository.commitStagingData(sessionId)

        val persons = db.personDao().getActivePersons()
        assertEquals(2, persons.size)
        assertEquals("Ali", persons[0].fullName)

        file.delete()
    }
}
