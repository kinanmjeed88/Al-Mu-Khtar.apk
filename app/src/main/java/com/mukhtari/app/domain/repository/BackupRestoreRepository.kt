package com.mukhtari.app.domain.repository

import java.io.File

interface BackupRestoreRepository {
    suspend fun createBackup(outputDir: File): File
    suspend fun restoreBackup(backupFile: File): Boolean
}
