package com.locallens

import android.app.Application
import android.util.Log
import com.locallens.di.AppModule.initAppModule

class LocalLensApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initAppModule(this)
    }
}
