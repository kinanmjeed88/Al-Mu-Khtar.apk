package com.mukhtari.app.di

import com.mukhtari.app.data.backup.BackupRestoreRepositoryImpl
import com.mukhtari.app.data.export.ImportExportRepositoryImpl
import com.mukhtari.app.data.repository.*
import com.mukhtari.app.data.security.SecurityRepositoryImpl
import com.mukhtari.app.domain.repository.*
import com.mukhtari.app.domain.usecase.ArabicNormalizationUseCase
import com.mukhtari.app.domain.usecase.DuplicateDetectionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import com.mukhtari.app.ui.regions.RegionsViewModel
import com.mukhtari.app.ui.transactions.TransactionsViewModel
import com.mukhtari.app.ui.letters.IncomingLettersViewModel
import com.mukhtari.app.ui.letters.OutgoingLettersViewModel
import com.mukhtari.app.ui.visitors.VisitorsViewModel
import com.google.gson.Gson

val appModule = module {
    // Dynamic Database Provider for Backup/Restore Room Lifecycle
    single { com.mukhtari.app.di.DatabaseProvider(androidContext()) }
    
    // Dynamically retrieve the active database instance.
    // Uses factory so DAOs don't hold a stale reference when DB restarts.
    factory { get<com.mukhtari.app.di.DatabaseProvider>().getDatabase() }

    // DAOs must be injected using `factory` instead of `single`
    // to ensure they always get the latest active database connection
    // if a Restore operation occurred.
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().regionDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().streetDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().alleyDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().personDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().familyDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().houseDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().residencyDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().dashboardDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().incomingLetterDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().outgoingLetterDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().visitorLogDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().transactionDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().activityLogDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().certificateDao() }
    factory { get<com.mukhtari.app.data.local.db.AppDatabase>().attachmentDao() }
    
    // Use cases
    single { ArabicNormalizationUseCase() }
    single { DuplicateDetectionUseCase(get()) }
    single { com.mukhtari.app.domain.usecase.PdfGeneratorUseCase(androidContext()) }
    single { com.mukhtari.app.domain.usecase.MergePersonsUseCase(get()) }
    
    // Infrastructure
    single { Gson() }
    
    // Repositories
    single<RegionRepository> { RegionRepositoryImpl(get(), get(), get()) }
    single<StreetRepository> { StreetRepositoryImpl(get(), get(), get()) }
    single<AlleyRepository> { AlleyRepositoryImpl(get(), get(), get()) }
    single<PersonRepository> { PersonRepositoryImpl(get(), get(), get(), get()) }
    single<FamilyRepository> { FamilyRepositoryImpl(get(), get(), get()) }
    single<HouseRepository> { HouseRepositoryImpl(get(), get(), get()) }
    single<ResidencyRepository> { ResidencyRepositoryImpl(get(), get(), get(), get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
    single<IncomingLetterRepository> { IncomingLetterRepositoryImpl(get(), get()) }
    single<OutgoingLetterRepository> { OutgoingLetterRepositoryImpl(get(), get()) }
    single<VisitorLogRepository> { VisitorLogRepositoryImpl(get(), get()) }
    
    single<SecurityRepository> { SecurityRepositoryImpl(androidContext()) }
    single<BackupRestoreRepository> { com.mukhtari.app.data.backup.BackupRestoreRepositoryImpl(androidContext(), get<com.mukhtari.app.di.DatabaseProvider>(), 1, 1) }
    single<ImportExportRepository> { ImportExportRepositoryImpl(get()) }
    single<ActivityLogRepository> { ActivityLogRepositoryImpl(get()) }
    single<CertificateRepository> { CertificateRepositoryImpl(get()) }

    // ViewModels
    viewModel { RegionsViewModel(get()) }
    viewModel { com.mukhtari.app.ui.families.FamiliesViewModel(get(), get(), get()) }
    viewModel { com.mukhtari.app.ui.persons.PersonsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { com.mukhtari.app.ui.certificates.ResidencyCertificateViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { com.mukhtari.app.ui.houses.HousesViewModel(get(), get(), get(), get()) }
    viewModel { com.mukhtari.app.ui.dashboard.DashboardViewModel(get()) }
    viewModel { com.mukhtari.app.ui.security.SecurityViewModel(get()) }
    viewModel { TransactionsViewModel(get()) }
    viewModel { IncomingLettersViewModel(get()) }
    viewModel { OutgoingLettersViewModel(get()) }
    viewModel { VisitorsViewModel(get()) }
    viewModel { com.mukhtari.app.ui.recyclebin.RecycleBinViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { com.mukhtari.app.ui.activitylog.ActivityLogViewModel(get()) }
    viewModel { com.mukhtari.app.ui.attachments.AttachmentsViewModel(get(), androidContext()) }
    viewModel { com.mukhtari.app.ui.backup.BackupRestoreViewModel(get()) }
}
