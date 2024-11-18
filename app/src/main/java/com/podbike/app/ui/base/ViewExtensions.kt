package com.podbike.app.ui.base

import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import kotlin.math.max

fun View.adjustEdgeToEdgePaddings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(
                top = max(systemBarsInsets.top, cutoutInsets.top),
                bottom = systemBarsInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }
}
fun View.adjustEdgeToEdgeMargins() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            view.updateLayoutParams<MarginLayoutParams> {
                topMargin = max(systemBarsInsets.top, cutoutInsets.top)
                bottomMargin = systemBarsInsets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }
}