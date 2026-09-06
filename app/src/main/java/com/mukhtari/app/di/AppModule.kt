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
import org.koin.androidx.viewmodel.dsl.viewModel
import com.mukhtari.app.ui.regions.RegionsViewModel
import com.mukhtari.app.ui.transactions.TransactionsViewModel
import com.mukhtari.app.ui.letters.IncomingLettersViewModel
import com.mukhtari.app.ui.letters.OutgoingLettersViewModel
import com.mukhtari.app.ui.visitors.VisitorsViewModel
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
    single { get<AppDatabase>().streetDao() }
    single { get<AppDatabase>().alleyDao() }
    single { get<AppDatabase>().personDao() }
    single { get<AppDatabase>().familyDao() }
    single { get<AppDatabase>().houseDao() }
    single { get<AppDatabase>().residencyDao() }
    single { get<AppDatabase>().dashboardDao() }
    single { get<AppDatabase>().incomingLetterDao() }
    single { get<AppDatabase>().outgoingLetterDao() }
    single { get<AppDatabase>().visitorLogDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().activityLogDao() }
    single { get<AppDatabase>().certificateDao() }
    single { get<AppDatabase>().attachmentDao() }
    
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
    single<BackupRestoreRepository> { BackupRestoreRepositoryImpl(androidContext(), get(), 1, 1) }
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
}
