package com.mukhtari.app.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.AttachmentEntity
import com.mukhtari.app.data.local.entity.RegionEntity
import com.mukhtari.app.di.DatabaseProvider
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import com.mukhtari.app.di.appModule
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
class BackupRestoreIntegrationTest : KoinTest {
    private lateinit var databaseProvider: DatabaseProvider
    private lateinit var context: Context
    private val dbName = "mukhtari_database"

    // Inject a Repository directly from Koin to verify graph traversal instead of direct DB access
    private val regionRepository: com.mukhtari.app.domain.repository.RegionRepository by inject()

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure clean state
        context.getDatabasePath(dbName).delete()
        File(context.getDatabasePath(dbName).path + "-wal").delete()
        File(context.getDatabasePath(dbName).path + "-shm").delete()

        stopKoin()
        startKoin {
            modules(appModule)
        }

        databaseProvider = DatabaseProvider(context)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        System.clearProperty("MUKHTARI_TEST_ABORT_REPLACEMENT")
        databaseProvider.closeDatabase()
        context.getDatabasePath(dbName).delete()
        File(context.getDatabasePath(dbName).path + "-wal").delete()
        File(context.getDatabasePath(dbName).path + "-shm").delete()
        stopKoin()
    }

    @Test
    fun testAtomicReplacementAndRollbackOnFailure() = runBlocking {
        // We use the Koin-injected repository here directly to verify the dependency graph connection

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
        regionRepository.saveRegion(regionA) // Uses Koin Repo -> DAO -> State A DB

        val repo = BackupRestoreRepositoryImpl(context, databaseProvider, 1, 1)

        val backupDir = File(context.cacheDir, "backup_test_atomic")
        backupDir.mkdirs()

        // Backup State A
        val backupFile = repo.createBackup(backupDir)
        assertTrue(backupFile.exists())

        // Modify the current database (State B) using Koin Repository
        val activeRegions = regionRepository.getActiveRegions()
        regionRepository.softDeleteRegion(activeRegions[0].id)
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
        regionRepository.saveRegion(regionB)

        val regionsStateB = regionRepository.getActiveRegions()
        assertEquals(1, regionsStateB.size)
        assertEquals("State B Region", regionsStateB[0].name)

        // To test TRUE replacement rollback, we need to fail during replacement,
        // after validation passes but before completion.
        // We will mock this via a custom system property trap read in the Repository
        System.setProperty("MUKHTARI_TEST_ABORT_REPLACEMENT", "true")

        val restoreResult = repo.restoreBackup(backupFile)
        assertFalse(restoreResult)

        // Clear trap
        System.clearProperty("MUKHTARI_TEST_ABORT_REPLACEMENT")

        // Verify Data after failed restore (Should remain State B) using Koin Repository again
        // If Koin singletons were stale, this would crash. It should cleanly return State B.
        val activeRegionsAfterFailure = regionRepository.getActiveRegions()
        assertEquals(1, activeRegionsAfterFailure.size)
        assertEquals("State B Region", activeRegionsAfterFailure[0].name) // DB rolled back cleanly to active State B

        // Finally, do a real restore back to State A
        val realRestoreResult = repo.restoreBackup(backupFile)
        assertTrue(realRestoreResult)

        // Verify Data after real restore using Koin Repository (Should return to State A)
        // If Koin was not successfully reloaded by the DatabaseProvider, this will crash with IllegalStateException
        val activeRegionsFinal = regionRepository.getActiveRegions()
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

        val repo = BackupRestoreRepositoryImpl(context, databaseProvider, 1, 1)
        val backupDir = File(context.cacheDir, "backup_test_missing_attachment")
        backupDir.mkdirs()

        val backupFile = repo.createBackup(backupDir)
        val restoreResult = repo.restoreBackup(backupFile)

        assertFalse("Restore should fail when required attachment is missing", restoreResult)

        backupDir.deleteRecursively()
    }
}
