package com.mukhtari.app.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.entity.ActivityLogEntity
import com.mukhtari.app.di.DatabaseProvider
import com.mukhtari.app.data.backup.BackupRestoreRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BackupRestoreActivityLogTest {
    private lateinit var databaseProvider: DatabaseProvider
    private lateinit var context: Context
    private val dbName = "mukhtari_database"

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure clean state
        context.getDatabasePath(dbName).delete()
        File(context.getDatabasePath(dbName).path + "-wal").delete()
        File(context.getDatabasePath(dbName).path + "-shm").delete()

        databaseProvider = DatabaseProvider(context)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        databaseProvider.closeDatabase()
        context.getDatabasePath(dbName).delete()
        File(context.getDatabasePath(dbName).path + "-wal").delete()
        File(context.getDatabasePath(dbName).path + "-shm").delete()
    }

    @Test
    fun testBackupAndRestoreLogsActivity() = runBlocking {
        val repo = BackupRestoreRepositoryImpl(context, databaseProvider, 1, 1)

        val backupDir = File(context.cacheDir, "backup_test_logs")
        backupDir.mkdirs()

        // Test Backup creates log
        val backupFile = repo.createBackup(backupDir)
        assertTrue(backupFile.exists())

        val dbAfterBackup = databaseProvider.getDatabase()
        var logs = dbAfterBackup.activityLogDao().getAllActivityLogs().first()
        assertEquals(1, logs.size)
        assertEquals("backup_created", logs[0].actionType)

        // Test Restore creates log
        val restoreResult = repo.restoreBackup(backupFile)
        assertTrue(restoreResult)

        val dbAfterRestore = databaseProvider.getDatabase()
        logs = dbAfterRestore.activityLogDao().getAllActivityLogs().first()
        assertEquals(2, logs.size)

        // Since we restored the DB from the moment backup_created was written,
        // the restored DB brings that log back. The new log "backup_restored" is appended.
        // Therefore, both logs should be present.
        assertTrue(logs.any { it.actionType == "backup_created" })
        assertTrue(logs.any { it.actionType == "backup_restored" })

        backupDir.deleteRecursively()
    }
}
