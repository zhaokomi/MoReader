package com.mochen.reader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoReaderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-level components here
    }
}
