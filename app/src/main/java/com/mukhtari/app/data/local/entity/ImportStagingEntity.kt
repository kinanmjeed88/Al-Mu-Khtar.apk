package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "import_staging",
    indices = [
        Index(value = ["import_session_id"])
    ]
)
data class ImportStagingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "import_session_id") val importSessionId: String,
    @ColumnInfo(name = "row_number") val rowNumber: Int,
    @ColumnInfo(name = "source_file_name") val sourceFileName: String,
    @ColumnInfo(name = "raw_data_json") val rawDataJson: String,
    @ColumnInfo(name = "normalized_data_json") val normalizedDataJson: String?,
    @ColumnInfo(name = "validation_status") val validationStatus: String,
    @ColumnInfo(name = "validation_errors") val validationErrors: String?,
    @ColumnInfo(name = "match_status") val matchStatus: String?,
    @ColumnInfo(name = "matched_entity_id") val matchedEntityId: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
