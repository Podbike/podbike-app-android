package com.podbike.app.ui

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.podbike.app.BuildConfig
import com.podbike.app.utils.CrashlyticsTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class PodbikeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupTimber()
        FirebaseCrashlytics.getInstance().sendUnsentReports()
    }

    private fun setupTimber() {
        if (BuildConfig.DEV || BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
            Timber.plant(Timber.DebugTree(), CrashlyticsTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

}