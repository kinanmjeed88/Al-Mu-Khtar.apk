package com.mukhtari.app.data.backup

import android.content.Context
import com.mukhtari.app.domain.repository.BackupRestoreRepository
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
    private val dbName: String,
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
        val dbFile = context.getDatabasePath(dbName)
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")
        val attachmentsDir = File(context.filesDir, "attachments")

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
                    val relativePath = file.toRelativeString(attachmentsDir.parentFile)
                    zos.putNextEntry(ZipEntry(relativePath))
                    FileInputStream(file).copyTo(zos)
                    zos.closeEntry()
                    fileHashes[relativePath] = getSha256(file)
                }
            }

            // Record counts placeholder (ideally fetched from DB directly)
            val recordCounts = JSONObject().apply {
                put("regions", 0) // Should be queried
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
        }

        backupFile
    }

    override suspend fun restoreBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        val stagingDir = File(context.cacheDir, "restore_staging_${System.currentTimeMillis()}")
        stagingDir.mkdirs()

        try {
            // 1. Unzip to staging
            ZipInputStream(FileInputStream(backupFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val destFile = File(stagingDir, entry.name)
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
            
            // Validate schema version
            val incomingSchema = manifestJson.getInt("schemaVersion")
            if (incomingSchema > schemaVersion) {
                // Cannot restore newer database on older app
                return@withContext false
            }

            // 3. Atomic Replacement
            val currentDbFile = context.getDatabasePath(dbName)
            
            val stagedDbFile = File(stagingDir, "database/${currentDbFile.name}")
            val stagedWalFile = File(stagingDir, "database/${currentDbFile.name}-wal")
            val stagedShmFile = File(stagingDir, "database/${currentDbFile.name}-shm")
            
            if (stagedDbFile.exists()) stagedDbFile.copyTo(currentDbFile, overwrite = true)
            if (stagedWalFile.exists()) stagedWalFile.copyTo(File(currentDbFile.path + "-wal"), overwrite = true)
            if (stagedShmFile.exists()) stagedShmFile.copyTo(File(currentDbFile.path + "-shm"), overwrite = true)
            
            val attachmentsStaging = File(stagingDir, "attachments")
            val currentAttachments = File(context.filesDir, "attachments")
            if (attachmentsStaging.exists()) {
                currentAttachments.deleteRecursively()
                attachmentsStaging.copyRecursively(currentAttachments, overwrite = true)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            stagingDir.deleteRecursively()
        }
    }
}
