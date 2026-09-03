package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "persons",
    foreignKeys = [
        ForeignKey(
            entity = FamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["family_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        ),
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
        Index(value = ["family_id"]),
        Index(value = ["house_id"]),
        Index(value = ["full_name"]),
        Index(value = ["is_deleted"])
    ]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "public_code") val publicCode: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "father_name") val fatherName: String?,
    @ColumnInfo(name = "grandfather_name") val grandfatherName: String?,
    val surname: String?,
    val gender: String,
    @ColumnInfo(name = "birth_date") val birthDate: String?,
    @ColumnInfo(name = "marital_status") val maritalStatus: String,
    @ColumnInfo(name = "relation_to_head") val relationToHead: String?,
    @ColumnInfo(name = "family_id") val familyId: Long?,
    @ColumnInfo(name = "house_id") val houseId: Long?,
    @ColumnInfo(name = "work_status") val workStatus: String,
    val employer: String?,
    @ColumnInfo(name = "job_title") val jobTitle: String?,
    @ColumnInfo(name = "education_level") val educationLevel: String,
    val phone: String?,
    @ColumnInfo(name = "phone_alt") val phoneAlt: String?,
    val notes: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "deleted_reason") val deletedReason: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
