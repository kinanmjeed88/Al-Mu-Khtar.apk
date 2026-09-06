package com.mukhtari.app.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.AttachmentEntity
import com.mukhtari.app.data.local.entity.RegionEntity
import com.mukhtari.app.di.DatabaseProvider
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
class BackupRestoreIntegrationTest {
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
    fun testAtomicReplacementAndRollbackOnFailure() = runBlocking {
        val db = databaseProvider.getDatabase()

        // State A (Initial Database)
        val regionA = RegionEntity(
            publicCode = "REG-A",
            governorate = "Baghdad",
            district = "Karkh",
            subDistrict = "Mansour",
            mahalla = "601",
            name = "State A Region",
            description = null,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.regionDao().insertRegion(regionA)

        val repo = BackupRestoreRepositoryImpl(context, databaseProvider, 1, 1)

        val backupDir = File(context.cacheDir, "backup_test_atomic")
        backupDir.mkdirs()

        // Backup State A
        val backupFile = repo.createBackup(backupDir)
        assertTrue(backupFile.exists())

        // Modify the current database (State B)
        db.regionDao().hardDeleteRegion(1L)
        val regionB = RegionEntity(
            publicCode = "REG-B",
            governorate = "Basra",
            district = "Abu Al-Khaseeb",
            subDistrict = "Abu Al-Khaseeb",
            mahalla = "101",
            name = "State B Region",
            description = null,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.regionDao().insertRegion(regionB)

        val regionsStateB = db.regionDao().getActiveRegions()
        assertEquals(1, regionsStateB.size)
        assertEquals("State B Region", regionsStateB[0].name)

        // Corrupt zip to trigger rollback
        val maliciousFile = File(backupDir, "wrong_hash.zip")
        backupFile.copyTo(maliciousFile)
        maliciousFile.appendBytes("corrupt".toByteArray()) // corrupt the zip

        val restoreResult = repo.restoreBackup(maliciousFile)
        assertFalse(restoreResult)

        // Verify Data after failed restore (Should remain State B)
        val dbAfterFailure = databaseProvider.getDatabase()
        val activeRegions = dbAfterFailure.regionDao().getActiveRegions()
        assertEquals(1, activeRegions.size)
        assertEquals("State B Region", activeRegions[0].name) // DB didn't get corrupted/overwritten

        // Finally, do a real restore
        val realRestoreResult = repo.restoreBackup(backupFile)
        assertTrue(realRestoreResult)

        // Verify Data after real restore (Should return to State A)
        val dbAfterRealRestore = databaseProvider.getDatabase()
        val activeRegionsFinal = dbAfterRealRestore.regionDao().getActiveRegions()
        assertEquals(1, activeRegionsFinal.size)
        assertEquals("State A Region", activeRegionsFinal[0].name)

        backupDir.deleteRecursively()
    }

    @Test
    fun restoreFailsWhenRequiredAttachmentIsMissing() = runBlocking {
        val db = databaseProvider.getDatabase()

        val attachment = AttachmentEntity(
            ownerType = "region",
            ownerId = 1L,
            fileType = "image",
            mimeType = "image/png",
            filePath = "test_file.png",
            fileName = "test_file.png",
            fileSize = 1024L,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.attachmentDao().insert(attachment)

        // We do NOT create the actual physical file, meaning it will be missing in the zip.

        val repo = BackupRestoreRepositoryImpl(context, databaseProvider, 1, 1)

        val backupDir = File(context.cacheDir, "backup_test_missing_attachment")
        backupDir.mkdirs()

        // Create the backup. It won't find the attachment physical file, so it won't zip it.
        // But the DB inside the zip *will* have the attachment record.
        val backupFile = repo.createBackup(backupDir)

        // Now, we try to restore this backup.
        // The restore process reads the DB, sees "test_file.png" is required (is_deleted=0),
        // and rejects the restore because the physical file is not in the staging dir.
        val restoreResult = repo.restoreBackup(backupFile)

        assertFalse("Restore should fail when required attachment is missing", restoreResult)

        backupDir.deleteRecursively()
    }
}
