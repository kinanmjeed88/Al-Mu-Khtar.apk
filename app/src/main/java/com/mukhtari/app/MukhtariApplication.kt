package com.mukhtari.app

import android.app.Application
import com.mukhtari.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MukhtariApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                val sw = StringWriter()
                exception.printStackTrace(PrintWriter(sw))
                val stackTrace = sw.toString()

                val logFile = File(cacheDir, "crash_log.txt")
                logFile.writeText("Crash on thread ${thread.name}:\n$stackTrace")
            } catch (e: Exception) {
                // Ignore failure to write log
            } finally {
                defaultHandler?.uncaughtException(thread, exception)
            }
        }
        
        startKoin {
            androidLogger()
            androidContext(this@MukhtariApplication)
            modules(appModule)
        }
    }
}
