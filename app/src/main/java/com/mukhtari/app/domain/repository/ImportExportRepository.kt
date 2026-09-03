package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.ImportStagingEntity
import java.io.File

interface ImportExportRepository {
    suspend fun parseExcelToStaging(file: File, sessionId: String): List<ImportStagingEntity>
    suspend fun validateStagingData(sessionId: String)
    suspend fun commitStagingData(sessionId: String)
    suspend fun exportDataToCsv(tableName: String, outputFile: File): Boolean
}
