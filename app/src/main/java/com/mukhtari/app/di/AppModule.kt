package com.mukhtari.app.di

import androidx.room.Room
import com.mukhtari.app.data.backup.BackupRestoreRepositoryImpl
import com.mukhtari.app.data.export.ImportExportRepositoryImpl
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.repository.*
import com.mukhtari.app.data.security.SecurityRepositoryImpl
import com.mukhtari.app.domain.repository.*
import com.mukhtari.app.domain.usecase.ArabicNormalizationUseCase
import com.mukhtari.app.domain.usecase.DuplicateDetectionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.google.gson.Gson

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mukhtari_database"
        ).build()
    }
    
    // DAOs
    single { get<AppDatabase>().regionDao() }
    single { get<AppDatabase>().personDao() }
    single { get<AppDatabase>().residencyDao() }
    single { get<AppDatabase>().dashboardDao() }
    
    // Use cases
    single { ArabicNormalizationUseCase() }
    single { DuplicateDetectionUseCase(get()) }
    
    // Infrastructure
    single { Gson() }
    
    // Repositories
    single<ResidencyRepository> { ResidencyRepositoryImpl(get(), get(), get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
    
    single<SecurityRepository> { SecurityRepositoryImpl(androidContext()) }
    single<BackupRestoreRepository> { BackupRestoreRepositoryImpl(androidContext(), "mukhtari_database", 1, 1) }
    single<ImportExportRepository> { ImportExportRepositoryImpl() }
}
