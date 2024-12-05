package com.podbike.app.ui.base

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

suspend fun <T> Flow<T>.collectWithErrorHandling(action: suspend (T) -> Unit) {
    this.catch { e ->
        Timber.e(e)
        FirebaseCrashlytics.getInstance().recordException(e)
    }.collect { value ->
        action(value)
    }
}