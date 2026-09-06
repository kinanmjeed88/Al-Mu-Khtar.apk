package com.mukhtari.app.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.RegionEntity
import kotlinx.coroutines.runBlocking
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
class BackupRestoreRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private val dbName = "test-backup-db-failure-paths"

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
    fun restoreFailsWhenZipIsCorrupt() = runBlocking {
        val repo = BackupRestoreRepositoryImpl(context, db, 1, 1)
        val backupDir = File(context.cacheDir, "backup_test_corrupt")
        backupDir.mkdirs()

        val corruptFile = File(backupDir, "corrupt.zip")
        corruptFile.writeText("This is not a valid zip file")

        val result = repo.restoreBackup(corruptFile)
        assertFalse("Restore should fail for a corrupt zip file", result)
    }

    @Test
    fun restoreFailsWhenZipSlipAttempted() = runBlocking {
        val repo = BackupRestoreRepositoryImpl(context, db, 1, 1)
        val backupDir = File(context.cacheDir, "backup_test_slip")
        backupDir.mkdirs()

        val maliciousFile = File(backupDir, "malicious.zip")
        ZipOutputStream(FileOutputStream(maliciousFile)).use { zos ->
            zos.putNextEntry(ZipEntry("../../../malicious_file.txt"))
            zos.write("Hacked".toByteArray())
            zos.closeEntry()
        }

        val result = repo.restoreBackup(maliciousFile)
        assertFalse("Restore should fail when Zip Slip is attempted", result)
    }

    @Test
    fun restoreFailsWhenManifestIsMissing() = runBlocking {
        val repo = BackupRestoreRepositoryImpl(context, db, 1, 1)
        val backupDir = File(context.cacheDir, "backup_test_missing_manifest")
        backupDir.mkdirs()

        val missingManifestFile = File(backupDir, "missing_manifest.zip")
        ZipOutputStream(FileOutputStream(missingManifestFile)).use { zos ->
            zos.putNextEntry(ZipEntry("database/dummy.db"))
            zos.write("dummy db".toByteArray())
            zos.closeEntry()
        }

        val result = repo.restoreBackup(missingManifestFile)
        assertFalse("Restore should fail when manifest is missing", result)
    }
}
