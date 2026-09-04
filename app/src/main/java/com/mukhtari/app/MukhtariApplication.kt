package com.mukhtari.app

import android.app.Application
import com.mukhtari.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MukhtariApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@MukhtariApplication)
            modules(appModule)
        }
    }
}
