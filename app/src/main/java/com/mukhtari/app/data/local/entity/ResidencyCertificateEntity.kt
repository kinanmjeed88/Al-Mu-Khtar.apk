package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "residency_certificates",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["transaction_id"], unique = true),
        Index(value = ["person_id"])
    ]
)
data class ResidencyCertificateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "transaction_id") val transactionId: Long,
    @ColumnInfo(name = "person_id") val personId: Long,
    @ColumnInfo(name = "snapshot_name") val snapshotName: String,
    @ColumnInfo(name = "snapshot_family") val snapshotFamily: String?,
    @ColumnInfo(name = "snapshot_house") val snapshotHouse: String?,
    @ColumnInfo(name = "snapshot_address") val snapshotAddress: String?,
    @ColumnInfo(name = "pdf_path") val pdfPath: String?,
    @ColumnInfo(name = "issued_at") val issuedAt: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
