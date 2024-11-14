package com.podbike.app.ui.navigation


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_LOCALE_SETTINGS
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IntentManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun openLocaleSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(ACTION_APP_LOCALE_SETTINGS)
        } else {
            Intent(ACTION_LOCALE_SETTINGS)
        }
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}