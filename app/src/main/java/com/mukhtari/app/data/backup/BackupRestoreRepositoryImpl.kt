package com.mukhtari.app.data.backup

import android.content.Context
import com.mukhtari.app.domain.repository.BackupRestoreRepository
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.ActivityLogEntity
import com.mukhtari.app.di.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupRestoreRepositoryImpl(
    private val context: Context,
    private val databaseProvider: DatabaseProvider,
    private val appVersionCode: Int,
    private val schemaVersion: Int
) : BackupRestoreRepository {

    private fun getSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    override suspend fun createBackup(outputDir: File): File = withContext(Dispatchers.IO) {
        val db = databaseProvider.getDatabase()
        val dbName = db.openHelper.databaseName ?: "mukhtari_database"

        // 1. Force WAL checkpoint to ensure all data is written to the main DB file
        db.query("PRAGMA wal_checkpoint(TRUNCATE)", null).use { cursor -> cursor.moveToFirst() }

        val dbFile = context.getDatabasePath(dbName)
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")
        val attachmentsDir = File(context.filesDir, "attachments")
        attachmentsDir.mkdirs()

        val backupFile = File(outputDir, "mukhtari_backup_${System.currentTimeMillis()}.zip")
        val fileHashes = mutableMapOf<String, String>()

        ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
            // Include db files
            listOf(dbFile, walFile, shmFile).forEach { file ->
                if (file.exists()) {
                    zos.putNextEntry(ZipEntry("database/${file.name}"))
                    FileInputStream(file).copyTo(zos)
                    zos.closeEntry()
                    fileHashes["database/${file.name}"] = getSha256(file)
                }
            }

            // Include attachments
            if (attachmentsDir.exists()) {
                attachmentsDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val relativePath = file.toRelativeString(attachmentsDir.parentFile!!)
                    zos.putNextEntry(ZipEntry(relativePath))
                    FileInputStream(file).copyTo(zos)
                    zos.closeEntry()
                    fileHashes[relativePath] = getSha256(file)
                }
            }

            val recordCounts = JSONObject().apply {
                db.query("SELECT COUNT(*) FROM regions", null).use { cursor -> if (cursor.moveToFirst()) put("regions", cursor.getInt(0)) }
                db.query("SELECT COUNT(*) FROM streets", null).use { cursor -> if (cursor.moveToFirst()) put("streets", cursor.getInt(0)) }
                db.query("SELECT COUNT(*) FROM alleys", null).use { cursor -> if (cursor.moveToFirst()) put("alleys", cursor.getInt(0)) }
                db.query("SELECT COUNT(*) FROM houses", null).use { cursor -> if (cursor.moveToFirst()) put("houses", cursor.getInt(0)) }
                db.query("SELECT COUNT(*) FROM families", null).use { cursor -> if (cursor.moveToFirst()) put("families", cursor.getInt(0)) }
                db.query("SELECT COUNT(*) FROM persons", null).use { cursor -> if (cursor.moveToFirst()) put("persons", cursor.getInt(0)) }
                db.query("SELECT COUNT(*) FROM attachments", null).use { cursor -> if (cursor.moveToFirst()) put("attachments", cursor.getInt(0)) }
            }

            val hashesJson = JSONObject().apply {
                fileHashes.forEach { (path, hash) -> put(path, hash) }
            }

            val manifestContent = JSONObject().apply {
                put("version", 1)
                put("appVersionCode", appVersionCode)
                put("schemaVersion", schemaVersion)
                put("timestamp", System.currentTimeMillis())
                put("recordCounts", recordCounts)
                put("fileHashes", hashesJson)
            }.toString()

            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestContent.toByteArray())
            zos.closeEntry()

            // Self-Validate the file we just created has valid hashes internally
            // (Normally happens on restore)
        }

        try {
            db.activityLogDao().insertLog(
                ActivityLogEntity(
                    actionType = "backup_created",
                    entityType = "system",
                    entityId = null,
                    description = "تم إنشاء نسخة احتياطية بنجاح",
                    oldValues = null,
                    newValues = null,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) { e.printStackTrace() }

        backupFile
    }

    override suspend fun restoreBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        val dbName = "mukhtari_database"
        val restoreTempDir = File(context.filesDir, "temp_restore")
        restoreTempDir.deleteRecursively()
        restoreTempDir.mkdirs()
        val stagingDir = File(restoreTempDir, "staging_${System.currentTimeMillis()}")
        stagingDir.mkdirs()

        try {
            // 1. Unzip to staging
            ZipInputStream(FileInputStream(backupFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val destFile = File(stagingDir, entry.name)
                    // Prevent Zip Slip
                    if (!destFile.canonicalPath.startsWith(stagingDir.canonicalPath)) {
                        throw SecurityException("Zip Slip detected: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        FileOutputStream(destFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            val manifestFile = File(stagingDir, "manifest.json")
            if (!manifestFile.exists()) return@withContext false

            val manifestJson = JSONObject(manifestFile.readText())
            val hashesJson = manifestJson.getJSONObject("fileHashes")

            // 2. Validate Integrity Hashes
            hashesJson.keys().forEach { path ->
                val expectedHash = hashesJson.getString(path)
                val extractedFile = File(stagingDir, path)
                if (!extractedFile.exists() || getSha256(extractedFile) != expectedHash) {
                    return@withContext false
                }
            }
            
            // Verify that all database files inside 'database/' match their hashes
            val databaseStagingDir = File(stagingDir, "database")
            if (databaseStagingDir.exists() && databaseStagingDir.isDirectory) {
                databaseStagingDir.listFiles()?.forEach { dbFile ->
                    val relativePath = "database/${dbFile.name}"
                    if (!hashesJson.has(relativePath)) {
                        return@withContext false
                    }
                    if (getSha256(dbFile) != hashesJson.getString(relativePath)) {
                        return@withContext false
                    }
                }
            }

            // Validate schema version
            val incomingSchema = manifestJson.getInt("schemaVersion")
            if (incomingSchema > schemaVersion) {
                // Cannot restore newer database on older app
                return@withContext false
            }

            // 2.5 Verify database integrity in staging before replacing
            val currentDbFile = context.getDatabasePath(dbName)
            val stagedDbFile = File(stagingDir, "database/${currentDbFile.name}")

            if (!stagedDbFile.exists()) {
                return@withContext false
            }

            // Attempt to open the staged database to verify integrity
            try {
                val dbRaw = android.database.sqlite.SQLiteDatabase.openDatabase(
                    stagedDbFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                dbRaw.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                    if (!cursor.moveToFirst() || cursor.getString(0) != "ok") {
                        dbRaw.close()
                        return@withContext false
                    }
                }

                // Read required attachment references to verify they exist in the extracted ZIP
                dbRaw.rawQuery("SELECT file_path FROM attachments WHERE is_deleted = 0", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val filePath = cursor.getString(0)
                        val stagedAttachment = File(stagingDir, "attachments/$filePath")
                        if (!stagedAttachment.exists()) {
                            dbRaw.close()
                            return@withContext false // Missing required attachment
                        }
                    }
                }

                dbRaw.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }

            // 3. Atomic Replacement
            val stagedWalFile = File(stagingDir, "database/${currentDbFile.name}-wal")
            val stagedShmFile = File(stagingDir, "database/${currentDbFile.name}-shm")
            
            // Close active DB connection before modifying files
            databaseProvider.closeDatabase()

            // Rollback mechanism
            val rollbackDir = File(restoreTempDir, "rollback")
            rollbackDir.mkdirs()

            val activeWalFile = File(currentDbFile.path + "-wal")
            val activeShmFile = File(currentDbFile.path + "-shm")

            if (currentDbFile.exists()) currentDbFile.copyTo(File(rollbackDir, currentDbFile.name), overwrite = true)
            if (activeWalFile.exists()) activeWalFile.copyTo(File(rollbackDir, activeWalFile.name), overwrite = true)
            if (activeShmFile.exists()) activeShmFile.copyTo(File(rollbackDir, activeShmFile.name), overwrite = true)
            
            val currentAttachments = File(context.filesDir, "attachments")
            val attachmentsRollback = File(rollbackDir, "attachments")
            if (currentAttachments.exists()) {
                currentAttachments.copyRecursively(attachmentsRollback, overwrite = true)
            }

            try {
                // Overwrite files
                if (stagedDbFile.exists()) stagedDbFile.copyTo(currentDbFile, overwrite = true)
                if (stagedWalFile.exists()) stagedWalFile.copyTo(activeWalFile, overwrite = true) else activeWalFile.delete()
                if (stagedShmFile.exists()) stagedShmFile.copyTo(activeShmFile, overwrite = true) else activeShmFile.delete()

                val attachmentsStaging = File(stagingDir, "attachments")
                currentAttachments.deleteRecursively()
                if (attachmentsStaging.exists()) {
                    attachmentsStaging.copyRecursively(currentAttachments, overwrite = true)
                }

                // Reopen the database after successful replacement
                val reopenedDb = databaseProvider.getDatabase()
                try {
                    reopenedDb.activityLogDao().insertLog(
                        ActivityLogEntity(
                            actionType = "backup_restored",
                            entityType = "system",
                            entityId = null,
                            description = "تم استعادة النسخة الاحتياطية بنجاح",
                            oldValues = null,
                            newValues = null,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) { e.printStackTrace() }

            } catch (e: Exception) {
                e.printStackTrace()
                // Rollback
                if (File(rollbackDir, currentDbFile.name).exists()) File(rollbackDir, currentDbFile.name).copyTo(currentDbFile, overwrite = true)
                if (File(rollbackDir, activeWalFile.name).exists()) File(rollbackDir, activeWalFile.name).copyTo(activeWalFile, overwrite = true)
                if (File(rollbackDir, activeShmFile.name).exists()) File(rollbackDir, activeShmFile.name).copyTo(activeShmFile, overwrite = true)
                if (attachmentsRollback.exists()) {
                    currentAttachments.deleteRecursively()
                    attachmentsRollback.copyRecursively(currentAttachments, overwrite = true)
                }

                // Reopen the original database
                databaseProvider.getDatabase()

                return@withContext false
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            restoreTempDir.deleteRecursively()
        }
    }
}
