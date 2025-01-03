package com.podbike.app.data

import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import com.podbike.app.ui.scanning.DeviceInfo

interface UserPreferences {
    fun saveSpeedUnit(speedUnit: SpeedUnit)
    fun getSpeedUnit(): SpeedUnit

    fun saveDistanceUnit(distanceUnit: DistanceUnit)
    fun getDistanceUnit(): DistanceUnit

    fun saveTemperatureUnit(temperatureUnit: TemperatureUnit)
    fun getTemperatureUnit(): TemperatureUnit

    fun addRecentDevice(deviceInfo: DeviceInfo)
    fun getRecentDevices(): List<DeviceInfo>
    fun getMostRecentDevice(): DeviceInfo?

    fun setTutorialCompleted(completed: Boolean)
    fun isTutorialCompleted(): Boolean

    fun setUpdateStartedFlag(updateStarted: Boolean, config: PodbikeDeviceMetadata? = null)

}