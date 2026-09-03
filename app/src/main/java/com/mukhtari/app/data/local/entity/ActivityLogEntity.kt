package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_log",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["action_type"])
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "action_type") val actionType: String,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: Long?,
    val description: String,
    @ColumnInfo(name = "old_values") val oldValues: String?,
    @ColumnInfo(name = "new_values") val newValues: String?,
    val timestamp: Long
)
