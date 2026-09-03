package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "houses",
    foreignKeys = [
        ForeignKey(
            entity = AlleyEntity::class,
            parentColumns = ["id"],
            childColumns = ["alley_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = StreetEntity::class,
            parentColumns = ["id"],
            childColumns = ["street_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["public_code"], unique = true),
        Index(value = ["internal_number"], unique = true),
        Index(value = ["alley_id"]),
        Index(value = ["street_id"]),
        Index(value = ["house_number"]),
        Index(value = ["is_deleted"])
    ]
)
data class HouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "public_code") val publicCode: String,
    @ColumnInfo(name = "internal_number") val internalNumber: String,
    @ColumnInfo(name = "house_number") val houseNumber: String,
    @ColumnInfo(name = "alley_id") val alleyId: Long?,
    @ColumnInfo(name = "street_id") val streetId: Long?,
    @ColumnInfo(name = "mahalla_number") val mahallaNumber: String?,
    @ColumnInfo(name = "detailed_address") val detailedAddress: String?,
    @ColumnInfo(name = "photo_path") val photoPath: String?,
    @ColumnInfo(name = "property_type") val propertyType: String,
    val status: String,
    @ColumnInfo(name = "ownership_type") val ownershipType: String,
    @ColumnInfo(name = "owner_name") val ownerName: String?,
    @ColumnInfo(name = "owner_phone") val ownerPhone: String?,
    val notes: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "deleted_reason") val deletedReason: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
