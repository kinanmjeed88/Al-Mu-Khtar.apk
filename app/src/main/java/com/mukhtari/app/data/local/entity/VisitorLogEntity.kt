package com.mukhtari.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "visitors_log"
)
data class VisitorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "visitor_name") val visitorName: String,
    val phone: String?,
    @ColumnInfo(name = "visit_reason") val visitReason: String,
    @ColumnInfo(name = "transaction_type") val transactionType: String?,
    @ColumnInfo(name = "visit_date") val visitDate: String,
    @ColumnInfo(name = "visit_time") val visitTime: String?,
    val result: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
