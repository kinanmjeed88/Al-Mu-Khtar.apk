package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streets",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["id"],
            childColumns = ["region_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["public_code"], unique = true),
        Index(value = ["region_id"]),
        Index(value = ["name"]),
        Index(value = ["is_deleted"])
    ]
)
data class StreetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "region_id") val regionId: Long,
    @ColumnInfo(name = "public_code") val publicCode: String,
    val name: String,
    val code: String?,
    val description: String?,
    val notes: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "deleted_reason") val deletedReason: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
