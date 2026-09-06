package com.mukhtari.app.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.backup.BackupRestoreRepositoryImpl
import com.mukhtari.app.data.local.entity.RegionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {
    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private val dbName = "test-backup-db"

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.databaseBuilder(
            context, AppDatabase::class.java, dbName
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
        context.getDatabasePath(dbName).delete()
    }

    @Test
    fun testBackupAndRestore() = runBlocking {
        // Insert a test record
        val region = RegionEntity(
            publicCode = "REG-01",
            governorate = "Baghdad",
            district = "Karkh",
            subDistrict = "Mansour",
            mahalla = "601",
            name = "Backup Test Region",
            description = null,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.regionDao().insertRegion(region)

        // Wait for WAL to checkpoint or close DB to ensure everything is written
        db.close()

        val repo = BackupRestoreRepositoryImpl(context, dbName, 1, 1)

        val backupDir = File(context.cacheDir, "backup_test")
        backupDir.mkdirs()

        val backupFile = repo.createBackup(backupDir)
        assertTrue(backupFile.exists())

        // Create new empty DB
        val emptyDb = Room.databaseBuilder(
            context, AppDatabase::class.java, dbName
        ).build()
        assertEquals(0, emptyDb.regionDao().getActiveRegions().size)
        emptyDb.close() // Must close before restore

        val restoreResult = repo.restoreBackup(backupFile)
        assertTrue(restoreResult)

        // Verify Data after restore
        val restoredDb = Room.databaseBuilder(
            context, AppDatabase::class.java, dbName
        ).build()
        val activeRegions = restoredDb.regionDao().getActiveRegions()
        assertEquals(1, activeRegions.size)
        assertEquals("Backup Test Region", activeRegions[0].name)
        restoredDb.close()

        backupDir.deleteRecursively()
    }
}
