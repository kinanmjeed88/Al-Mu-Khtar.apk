package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "incoming_letters",
    indices = [
        Index(value = ["public_code"], unique = true),
        Index(value = ["is_deleted"])
    ]
)
data class IncomingLetterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "public_code") val publicCode: String,
    @ColumnInfo(name = "letter_number") val letterNumber: String,
    @ColumnInfo(name = "letter_date") val letterDate: String,
    val sender: String,
    val subject: String,
    val details: String?,
    @ColumnInfo(name = "required_action") val requiredAction: String?,
    val status: String,
    @ColumnInfo(name = "action_date") val actionDate: String?,
    val notes: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "deleted_reason") val deletedReason: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
