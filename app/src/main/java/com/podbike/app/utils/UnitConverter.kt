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
        metersPerSecond: Int,
        speedUnit: SpeedUnit,
        decimalPlaces: Int
    ): String {
        val result = when (speedUnit) {
            SpeedUnit.KILOMETERS_PER_HOUR -> metersPerSecond * METERS_PER_SECOND_TO_KMH
            SpeedUnit.MILES_PER_HOUR -> metersPerSecond * METERS_PER_SECOND_TO_MPH
            SpeedUnit.METERS_PER_SECOND -> metersPerSecond
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
        private const val METERS_PER_SECOND_TO_KMH = 3.6
        private const val METERS_PER_SECOND_TO_MPH = 2.23694
    }

}