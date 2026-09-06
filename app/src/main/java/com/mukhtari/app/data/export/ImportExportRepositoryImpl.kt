package com.mukhtari.app.data.export

import android.content.Context
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.ImportStagingEntity
import com.mukhtari.app.domain.repository.ImportExportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class ImportExportRepositoryImpl(
    private val db: AppDatabase
) : ImportExportRepository {

    override suspend fun parseExcelToStaging(file: File, sessionId: String): List<ImportStagingEntity> = withContext(Dispatchers.IO) {
        val stagingEntities = mutableListOf<ImportStagingEntity>()
        var rowNumber = 1

        if (file.extension.equals("csv", ignoreCase = true)) {
            BufferedReader(FileReader(file)).use { reader ->
                reader.readLine() // skip header
                var line = reader.readLine()
                while (line != null) {
                    val cells = line.split(",")
                    val rowJson = JSONObject()
                    cells.forEachIndexed { index, value ->
                        rowJson.put("col_$index", value.trim())
                    }
                    
                    stagingEntities.add(createStagingEntity(sessionId, rowNumber, file.name, rowJson))
                    rowNumber++
                    line = reader.readLine()
                }
            }
        } else if (file.extension.equals("xlsx", ignoreCase = true) || file.extension.equals("xls", ignoreCase = true)) {
            FileInputStream(file).use { fis ->
                val workbook = WorkbookFactory.create(fis)
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val rowJson = JSONObject()
                    for (j in 0 until (headerRow?.lastCellNum ?: 0)) {
                        val cell = row.getCell(j, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        rowJson.put("col_$j", cell.toString().trim())
                    }
                    stagingEntities.add(createStagingEntity(sessionId, rowNumber, file.name, rowJson))
                    rowNumber++
                }
                workbook.close()
            }
        }
        
        db.importStagingDao().insertBatch(stagingEntities)
        stagingEntities
    }

    private fun createStagingEntity(sessionId: String, rowNumber: Int, fileName: String, rowJson: JSONObject): ImportStagingEntity {
        return ImportStagingEntity(
            importSessionId = sessionId,
            rowNumber = rowNumber,
            sourceFileName = fileName,
            rawDataJson = rowJson.toString(),
            normalizedDataJson = null,
            validationStatus = "pending",
            validationErrors = null,
            matchStatus = null,
            matchedEntityId = null,
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun validateStagingData(sessionId: String) = withContext(Dispatchers.IO) {
        val records = db.importStagingDao().getStagingBySessionId(sessionId)
        
        records.forEach { record ->
            val isValid = record.rawDataJson.contains("col_0") 
            val updatedRecord = record.copy(
                validationStatus = if (isValid) "valid" else "error",
                validationErrors = if (isValid) null else "Missing required primary column data"
            )
            db.importStagingDao().updateStagingRecord(updatedRecord)
        }
    }

    override suspend fun commitStagingData(sessionId: String) {
        db.withTransaction {
            val records = db.importStagingDao().getStagingBySessionId(sessionId)
            val validRecords = records.filter { it.validationStatus == "valid" }

            for (record in validRecords) {
                try {
                    val rowJson = JSONObject(record.rawDataJson)
                    // Simplified import mapper for PersonEntity logic depending on structure.
                    // Assumes col_0 = fullName, col_1 = publicCode...
                    val fullName = rowJson.optString("col_0", "").trim()
                    if (fullName.isNotEmpty()) {
                        val person = com.mukhtari.app.data.local.entity.PersonEntity(
                            publicCode = java.util.UUID.randomUUID().toString().take(8).uppercase(),
                            fullName = fullName,
                            fatherName = rowJson.optString("col_1", "").trim().takeIf { it.isNotEmpty() },
                            grandfatherName = null,
                            surname = null,
                            gender = "unknown",
                            birthDate = null,
                            maritalStatus = "unknown",
                            relationToHead = null,
                            familyId = null,
                            houseId = null,
                            workStatus = "unknown",
                            employer = null,
                            jobTitle = null,
                            educationLevel = "unknown",
                            phone = null,
                            phoneAlt = null,
                            notes = null,
                            isDeleted = 0,
                            deletedAt = null,
                            deletedReason = null,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        db.personDao().insertPerson(person)

                        // Log activity
                        db.activityLogDao().insertLog(
                            com.mukhtari.app.data.local.entity.ActivityLogEntity(
                                actionType = "IMPORT",
                                entityType = "Person",
                                entityId = null,
                                description = "Imported person $fullName via session $sessionId",
                                oldValues = null,
                                newValues = person.toString(),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            db.importStagingDao().deleteStagingSession(sessionId)
        }
    }

    override suspend fun exportDataToCsv(tableName: String, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val query = SupportSQLiteQueryBuilder.builder("SELECT * FROM $tableName WHERE is_deleted = 0").create()
            val cursor = db.query(query)
            
            OutputStreamWriter(FileOutputStream(outputFile)).use { writer ->
                // Write Header
                val columnNames = cursor.columnNames
                writer.write(columnNames.joinToString(",") + "\n")
                
                // Write Rows
                while (cursor.moveToNext()) {
                    val rowValues = mutableListOf<String>()
                    for (i in 0 until cursor.columnCount) {
                        val value = cursor.getString(i) ?: ""
                        rowValues.add("\"" + value.replace("\"", "\"\"") + "\"")
                    }
                    writer.write(rowValues.joinToString(",") + "\n")
                }
            }
            cursor.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
