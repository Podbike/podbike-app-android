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

package com.podbike.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.podbike.app.data.*
import com.podbike.app.ui.scanning.DeviceInfo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserPreferencesImplTest {

    private lateinit var userPreferences: UserPreferences
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userPreferences = UserPreferencesImpl(context)
        context.getSharedPreferences(UserPreferencesImpl.PREF_NAME, Context.MODE_PRIVATE).edit()
            .clear().apply()
    }

    @Test
    fun testAddRecentDevice() {
        val device = DeviceInfo(address = "1234", name = "Test Device")
        userPreferences.addRecentDevice(device)
        val devices = userPreferences.getRecentDevices()
        assertEquals(1, devices.size)
        assertEquals(device, devices[0])
    }

    @Test
    fun testGetMostRecentDevice() {
        val device1 = DeviceInfo(address = "1234", name = "Test Device 1")
        val device2 = DeviceInfo(address = "5678", name = "Test Device 2")
        userPreferences.addRecentDevice(device1)
        userPreferences.addRecentDevice(device2)
        val mostRecentDevice = userPreferences.getMostRecentDevice()
        assertEquals(device2, mostRecentDevice)
    }

    @Test
    fun testNoDuplicateDevices() {
        val device = DeviceInfo(address = "1234", name = "Test Device")
        userPreferences.addRecentDevice(device)
        userPreferences.addRecentDevice(device)
        val devices = userPreferences.getRecentDevices()
        assertEquals(1, devices.size)
        assertEquals(device, devices[0])
    }

    @Test
    fun testSaveAndRetrieveSpeedUnit() {
        userPreferences.saveSpeedUnit(SpeedUnit.MILES_PER_HOUR)
        val speedUnit = userPreferences.getSpeedUnit()
        assertEquals(SpeedUnit.MILES_PER_HOUR, speedUnit)
    }

    @Test
    fun testSaveAndRetrieveDistanceUnit() {
        userPreferences.saveDistanceUnit(DistanceUnit.MILES)
        val distanceUnit = userPreferences.getDistanceUnit()
        assertEquals(DistanceUnit.MILES, distanceUnit)
    }

    @Test
    fun testSaveAndRetrieveTemperatureUnit() {
        userPreferences.saveTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        val temperatureUnit = userPreferences.getTemperatureUnit()
        assertEquals(TemperatureUnit.FAHRENHEIT, temperatureUnit)
    }
}