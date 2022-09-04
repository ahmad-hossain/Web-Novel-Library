package com.github.godspeed010.weblib

import android.app.Application
import timber.log.Timber

class WebLibApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}