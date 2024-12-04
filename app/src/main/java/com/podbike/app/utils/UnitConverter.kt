package com.podbike.app.utils

import com.podbike.app.data.DistanceUnit
import com.podbike.app.data.SpeedUnit

class UnitConverter() {

    fun convertDistance(meters: Float, distanceUnit: DistanceUnit, decimalPlaces: Int): String {
        val result = when (distanceUnit) {
            DistanceUnit.KILOMETERS -> meters / METERS_IN_KILOMETER
            DistanceUnit.MILES -> meters / METERS_IN_MILE
            DistanceUnit.METERS -> meters
        }
        return String.format("%.${decimalPlaces}f", result)
    }

    fun convertSpeed(
        kilometersPerHour: Float,
        speedUnit: SpeedUnit,
        decimalPlaces: Int
    ): String {
        val result = when (speedUnit) {
            SpeedUnit.KILOMETERS_PER_HOUR -> kilometersPerHour
            SpeedUnit.MILES_PER_HOUR -> KILOMETERS_PER_HOUR_TO_MPH * kilometersPerHour
            SpeedUnit.METERS_PER_SECOND -> KILOMETERS_PER_HOUR_TO_MPS * kilometersPerHour
        }
        return String.format("%.${decimalPlaces}f", result)
    }

    fun getDistanceUnitAbbreviation(distanceUnit: DistanceUnit): String {
        return when (distanceUnit) {
            DistanceUnit.KILOMETERS -> "km"
            DistanceUnit.MILES -> "mi"
            DistanceUnit.METERS -> "m"
        }
    }

    fun getSpeedUnitAbbreviation(speedUnit: SpeedUnit): String {
        return when (speedUnit) {
            SpeedUnit.KILOMETERS_PER_HOUR -> "km/h"
            SpeedUnit.MILES_PER_HOUR -> "mph"
            SpeedUnit.METERS_PER_SECOND -> "m/s"
        }
    }

    companion object {
        private const val METERS_IN_MILE = 1609.344
        private const val METERS_IN_KILOMETER = 1000
        private const val KILOMETERS_PER_HOUR_TO_MPH = 0.62137
        private const val KILOMETERS_PER_HOUR_TO_MPS = 0.2778
    }

}