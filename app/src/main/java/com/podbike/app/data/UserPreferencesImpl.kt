package com.podbike.app.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesImpl(context: Context) : UserPreferences {

    companion object {
        const val PREF_NAME = "user_preferences"
        const val KEY_SPEED_UNIT = "speed_unit"
        const val KEY_DISTANCE_UNIT = "distance_unit"
        const val KEY_TEMPERATURE_UNIT = "temperature_unit"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun saveSpeedUnit(speedUnit: SpeedUnit) {
        sharedPreferences.edit().putString(KEY_SPEED_UNIT, speedUnit.name).apply()
    }

    override fun getSpeedUnit(): SpeedUnit {
        val speedUnitName =
            sharedPreferences.getString(KEY_SPEED_UNIT, SpeedUnit.KILOMETERS_PER_HOUR.name)
        return SpeedUnit.valueOf(speedUnitName!!)
    }

    override fun saveDistanceUnit(distanceUnit: DistanceUnit) {
        sharedPreferences.edit().putString(KEY_DISTANCE_UNIT, distanceUnit.name).apply()
    }

    override fun getDistanceUnit(): DistanceUnit {
        val distanceUnitName =
            sharedPreferences.getString(KEY_DISTANCE_UNIT, DistanceUnit.KILOMETERS.name)
        return DistanceUnit.valueOf(distanceUnitName!!)
    }

    override fun saveTemperatureUnit(temperatureUnit: TemperatureUnit) {
        sharedPreferences.edit().putString(KEY_TEMPERATURE_UNIT, temperatureUnit.name).apply()
    }

    override fun getTemperatureUnit(): TemperatureUnit {
        val temperatureUnitName =
            sharedPreferences.getString(KEY_TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.name)
        return TemperatureUnit.valueOf(temperatureUnitName!!)
    }
}