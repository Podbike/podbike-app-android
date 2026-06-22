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

package com.podbike.app.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionManager @Inject constructor(private val activity: Activity) {

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    fun initializePermissionLauncher(
        requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    ) {
        this.requestPermissionsLauncher = requestPermissionsLauncher
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothScanPermission =
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission =
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
            bluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
        } else {
            val bluetoothPermission =
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH)
            val bluetoothAdminPermission =
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN)
            bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED &&
                    hasLocationPermission()
        }
    }

    fun hasLocationPermission(): Boolean {
        val locationPermission =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        return locationPermission == PackageManager.PERMISSION_GRANTED
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        requestPermissionsLauncher.launch(permissions)
    }
}