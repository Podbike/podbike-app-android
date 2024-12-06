package com.podbike.app.ui.statistics

data class StatisticsDataUiModel(
    val interiorTemperature: Int,
    val temperatureUnit: String,
    val co2Saved: Float,
    val totalDistance: String,
    val distanceUnit: String,
    val currentTripTime: Int,
    val averageSpeed: String,
    val averageSpeedUnit: String,
    val averageRpm: Int,
    val powerGenerated: Int,
    val batteryRemaining: Int,
)