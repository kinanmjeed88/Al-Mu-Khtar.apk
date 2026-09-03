package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "families",
    foreignKeys = [
        ForeignKey(
            entity = HouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["house_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["public_code"], unique = true),
        Index(value = ["family_code"], unique = true),
        Index(value = ["house_id"]),
        Index(value = ["head_of_family_id"]),
        Index(value = ["is_deleted"])
    ]
)
data class FamilyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "public_code") val publicCode: String,
    @ColumnInfo(name = "family_code") val familyCode: String,
    @ColumnInfo(name = "family_name") val familyName: String?,
    @ColumnInfo(name = "house_id") val houseId: Long?,
    @ColumnInfo(name = "head_of_family_id") val headOfFamilyId: Long?,
    @ColumnInfo(name = "residency_date") val residencyDate: String?,
    @ColumnInfo(name = "residency_status") val residencyStatus: String,
    @ColumnInfo(name = "info_source") val infoSource: String?,
    val notes: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "deleted_reason") val deletedReason: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
