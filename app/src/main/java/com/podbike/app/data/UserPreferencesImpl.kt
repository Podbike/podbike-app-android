package com.podbike.app.data

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.os.Build

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
            sharedPreferences.getString(KEY_SPEED_UNIT, getDefaultSpeedUnit().name)
        return SpeedUnit.valueOf(speedUnitName!!)
    }

    override fun saveDistanceUnit(distanceUnit: DistanceUnit) {
        sharedPreferences.edit().putString(KEY_DISTANCE_UNIT, distanceUnit.name).apply()
    }

    override fun getDistanceUnit(): DistanceUnit {
        val distanceUnitName =
            sharedPreferences.getString(KEY_DISTANCE_UNIT, getDefaultDistanceUnit().name)
        return DistanceUnit.valueOf(distanceUnitName!!)
    }

    override fun saveTemperatureUnit(temperatureUnit: TemperatureUnit) {
        sharedPreferences.edit().putString(KEY_TEMPERATURE_UNIT, temperatureUnit.name).apply()
    }

    override fun getTemperatureUnit(): TemperatureUnit {
        val temperatureUnitName =
            sharedPreferences.getString(KEY_TEMPERATURE_UNIT, getDefaultTemperatureUnit().name)
        return TemperatureUnit.valueOf(temperatureUnitName!!)
    }

    private fun getDefaultSpeedUnit(): SpeedUnit {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (LocaleData.getMeasurementSystem(ULocale.getDefault())) {
                LocaleData.MeasurementSystem.US -> SpeedUnit.MILES_PER_HOUR
                LocaleData.MeasurementSystem.SI, LocaleData.MeasurementSystem.UK -> SpeedUnit.KILOMETERS_PER_HOUR
                else -> SpeedUnit.KILOMETERS_PER_HOUR
            }
        } else {
            SpeedUnit.KILOMETERS_PER_HOUR
        }
    }

    private fun getDefaultDistanceUnit(): DistanceUnit {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (LocaleData.getMeasurementSystem(ULocale.getDefault())) {
                LocaleData.MeasurementSystem.US, LocaleData.MeasurementSystem.UK -> DistanceUnit.MILES
                LocaleData.MeasurementSystem.SI -> DistanceUnit.KILOMETERS
                else -> DistanceUnit.KILOMETERS
            }
        } else {
            DistanceUnit.KILOMETERS
        }
    }

    private fun getDefaultTemperatureUnit(): TemperatureUnit {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (LocaleData.getMeasurementSystem(ULocale.getDefault())) {
                LocaleData.MeasurementSystem.US -> TemperatureUnit.FAHRENHEIT
                LocaleData.MeasurementSystem.SI, LocaleData.MeasurementSystem.UK -> TemperatureUnit.CELSIUS
                else -> TemperatureUnit.CELSIUS
            }
        } else {
            TemperatureUnit.CELSIUS
        }
    }

}