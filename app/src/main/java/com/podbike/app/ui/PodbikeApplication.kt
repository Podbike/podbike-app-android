package com.podbike.app.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PodbikeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}