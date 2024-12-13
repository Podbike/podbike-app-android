package com.podbike.app.ui.statistics

data class StatisticsDataUiModel(
    val interiorTemperature: String,
    val temperatureUnit: String,
    val co2Saved: Float,
    val totalDistance: String,
    val distanceUnit: String,
    val currentTripTime: Int,
    val averageSpeed: String,
    val speedUnit: String,
    val averageRpm: Int,
    val powerGenerated: Int,
    val batteryRemaining: Int,
    val maxSpeed: String,
) {
    companion object {
        val empty = StatisticsDataUiModel(
            interiorTemperature = "",
            temperatureUnit = "",
            co2Saved = 0f,
            totalDistance = "",
            distanceUnit = "",
            currentTripTime = 0,
            averageSpeed = "0",
            speedUnit = "",
            averageRpm = 0,
            powerGenerated = 0,
            batteryRemaining = 0,
            maxSpeed = "",
        )
    }

}