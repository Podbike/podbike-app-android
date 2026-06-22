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