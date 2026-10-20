package com.example.dictionary

import android.app.Application
import com.example.dictionary.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaseApp: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }
    }

}