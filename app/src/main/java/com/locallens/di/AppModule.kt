package com.locallens.di

import android.content.Context
import com.locallens.BuildConfig

object AppModule {

    fun initAppModule(applicationContext: Context) {
        if (BuildConfig.API_KEY.isEmpty()) {
            android.util.Log.w("LocalLens", "API key is missing — add it to local.properties")
        }
    }
}
