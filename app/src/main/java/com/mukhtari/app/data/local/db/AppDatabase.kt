package com.mukhtari.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mukhtari.app.data.local.dao.*
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
    abstract fun streetDao(): StreetDao
    abstract fun alleyDao(): AlleyDao
    abstract fun houseDao(): HouseDao
    abstract fun familyDao(): FamilyDao
    abstract fun personDao(): PersonDao
    abstract fun residencyDao(): ResidencyDao
    abstract fun personMergeLogDao(): PersonMergeLogDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun transactionDao(): TransactionDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun importStagingDao(): ImportStagingDao
    abstract fun certificateDao(): CertificateDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun incomingLetterDao(): IncomingLetterDao
    abstract fun outgoingLetterDao(): OutgoingLetterDao
    abstract fun visitorLogDao(): VisitorLogDao
}
