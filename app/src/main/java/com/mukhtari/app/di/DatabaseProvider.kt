package com.mukhtari.app.di

import android.content.Context
import androidx.room.Room
import com.mukhtari.app.data.local.db.AppDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Handles the Room Database lifecycle securely, allowing the database to be closed
 * and re-opened dynamically during a restore operation, and automatically invalidating
 * Koin's DAO singletons by reloading the database module.
 */
class DatabaseProvider(private val context: Context) : KoinComponent {
    private var database: AppDatabase? = null

    private val _dbState = MutableStateFlow<Boolean>(false)
    val dbState: StateFlow<Boolean> = _dbState

    @Synchronized
    fun getDatabase(): AppDatabase {
        if (database == null || !database!!.isOpen) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mukhtari_database"
            ).build()
            _dbState.value = true
        }
        return database!!
    }

    @Synchronized
    fun closeDatabase() {
        database?.close()
        database = null
        _dbState.value = false
    }

    @Synchronized
    fun reloadDependencies() {
        // Unload and immediately reload the App module. Because repositories and ViewModels are injected
        // eagerly or lazily, unloading/loading forces them to acquire the newly constructed Database
        // instance upon their next invocation instead of relying on the closed SQLite handle.
        unloadKoinModules(appModule)
        loadKoinModules(appModule)
    }
}
