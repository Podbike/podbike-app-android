package com.podbike.app.data

interface UserPreferences {
    fun saveSpeedUnit(speedUnit: SpeedUnit)
    fun getSpeedUnit(): SpeedUnit

    fun saveDistanceUnit(distanceUnit: DistanceUnit)
    fun getDistanceUnit(): DistanceUnit

    fun saveTemperatureUnit(temperatureUnit: TemperatureUnit)
    fun getTemperatureUnit(): TemperatureUnit
}