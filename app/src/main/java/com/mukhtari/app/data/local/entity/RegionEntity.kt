package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "regions",
    indices = [
        Index(value = ["public_code"], unique = true)
    ]
)
data class RegionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "public_code") val publicCode: String,
    val governorate: String,
    val district: String,
    @ColumnInfo(name = "sub_district") val subDistrict: String,
    val mahalla: String,
    val name: String,
    val description: String?,
    val notes: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "deleted_reason") val deletedReason: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
