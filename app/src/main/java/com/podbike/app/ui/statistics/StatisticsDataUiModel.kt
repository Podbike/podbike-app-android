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

package com.podbike.app.ui.statistics

data class StatisticsDataUiModel(
    val interiorTemperature: String,
    val temperatureUnit: String,
    val co2Saved: Float,
    val totalDistance: String,
    val distanceUnit: String,
    val currentTripTime: Int,
    val averageSpeed: String,
    val averageTripSpeed: String,
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
            averageTripSpeed = "0"
        )
    }

}