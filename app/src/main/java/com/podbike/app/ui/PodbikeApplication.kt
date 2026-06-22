/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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