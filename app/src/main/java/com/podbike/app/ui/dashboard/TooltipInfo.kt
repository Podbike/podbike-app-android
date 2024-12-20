package com.podbike.app.ui.dashboard

import android.view.Gravity

data class TooltipInfo(
    val widgetId: Int,
    val title: String,
    val message: String,
    val gravity: Int = Gravity.BOTTOM
)
