package com.mukhtari.app.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.db.AppDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class BackupRestoreRepositoryImplTest2 {

    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private val dbName = "test-backup-db-failure-paths-2"

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.databaseBuilder(
            context, AppDatabase::class.java, dbName
        ).build()
    }

    @After
    fun closeDb() {
        db.close()
        context.getDatabasePath(dbName).delete()
    }

    @Test
    fun restoreFailsWhenHashIsWrong() = runBlocking {
        val repo = BackupRestoreRepositoryImpl(context, db, 1, 1)
        val backupDir = File(context.cacheDir, "backup_test_wrong_hash")
        backupDir.mkdirs()

        val maliciousFile = File(backupDir, "wrong_hash.zip")
        ZipOutputStream(FileOutputStream(maliciousFile)).use { zos ->
            zos.putNextEntry(ZipEntry("database/dummy.db"))
            zos.write("dummy db".toByteArray())
            zos.closeEntry()

            val manifestContent = JSONObject().apply {
                put("version", 1)
                put("appVersionCode", 1)
                put("schemaVersion", 1)
                put("timestamp", System.currentTimeMillis())
                put("recordCounts", JSONObject())
                put("fileHashes", JSONObject().apply {
                    put("database/dummy.db", "invalid_hash_value")
                })
            }.toString()

            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestContent.toByteArray())
            zos.closeEntry()
        }

        val result = repo.restoreBackup(maliciousFile)
        assertFalse("Restore should fail when hash is wrong", result)
    }

    @Test
    fun restoreFailsWhenSchemaVersionIsNewer() = runBlocking {
        val repo = BackupRestoreRepositoryImpl(context, db, 1, 1)
        val backupDir = File(context.cacheDir, "backup_test_wrong_schema")
        backupDir.mkdirs()

        val maliciousFile = File(backupDir, "wrong_schema.zip")
        ZipOutputStream(FileOutputStream(maliciousFile)).use { zos ->
            // Use dummy db file
            zos.putNextEntry(ZipEntry("database/dummy.db"))
            zos.write("dummy db".toByteArray())
            zos.closeEntry()

            val manifestContent = JSONObject().apply {
                put("version", 1)
                put("appVersionCode", 1)
                put("schemaVersion", 999) // Way newer schema version
                put("timestamp", System.currentTimeMillis())
                put("recordCounts", JSONObject())
                put("fileHashes", JSONObject().apply {
                    // Actual sha256 of "dummy db" is 68df32f14c2b993693fb13328ebccbd61899bf95832bd1a7090b8e6ea47d95d1
                    put("database/dummy.db", "68df32f14c2b993693fb13328ebccbd61899bf95832bd1a7090b8e6ea47d95d1")
                })
            }.toString()

            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestContent.toByteArray())
            zos.closeEntry()
        }

        val result = repo.restoreBackup(maliciousFile)
        assertFalse("Restore should fail when schema version is newer", result)
    }
}
