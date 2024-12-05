package com.podbike.app.ui.statistics

import com.podbike.app.data.bluetooth.model.PodbikeLightStatus

data class StatisticsDataUiModel(
    val interiorTemperature: Int,
    val co2Saved: Float,
    val totalDistance: Float,
    val currentTripTime: Int,
    val averageSpeed: Int,
    val averageRpm: Int,
    val powerGenerated: Int,
    val batteryRemaining: Int,
)