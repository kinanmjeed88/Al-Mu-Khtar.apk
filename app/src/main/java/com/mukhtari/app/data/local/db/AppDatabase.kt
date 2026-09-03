package com.mukhtari.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mukhtari.app.data.local.dao.PersonDao
import com.mukhtari.app.data.local.dao.RegionDao
import com.mukhtari.app.data.local.dao.ResidencyDao
import com.mukhtari.app.data.local.entity.*

@Database(
    entities = [
        RegionEntity::class,
        StreetEntity::class,
        AlleyEntity::class,
        HouseEntity::class,
        FamilyEntity::class,
        PersonEntity::class,
        ResidencyEntity::class,
        TransactionEntity::class,
        ResidencyCertificateEntity::class,
        IncomingLetterEntity::class,
        OutgoingLetterEntity::class,
        VisitorLogEntity::class,
        AttachmentEntity::class,
        ActivityLogEntity::class,
        PersonMergeLogEntity::class,
        ImportStagingEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun regionDao(): RegionDao
    abstract fun residencyDao(): ResidencyDao
    abstract fun personDao(): PersonDao
}
