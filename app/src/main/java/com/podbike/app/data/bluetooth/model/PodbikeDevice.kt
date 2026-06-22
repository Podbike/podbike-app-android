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

package com.podbike.app.data.bluetooth.model

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import timber.log.Timber
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * A Wrapper class over ClientBleGatt for easy access to the methods specific for Podbike.
 */

class PodbikeDevice(val client: ClientBleGatt, val device: DeviceInfo) {

    private var services: ClientBleGattServices? = null

    val data = PodbikeDeviceData(this, device)
    private val operationMutex = Mutex()

    suspend fun discoverServices() {
        services = client.discoverServices()
    }

    fun isConnected(): Flow<Boolean> =
        client.connectionState.map { it == GattConnectionState.STATE_CONNECTED }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun readCharacteristic(
        characteristicId: UUID,
        serviceId: UUID = HaarekBoardSpec.PODBIKE_SERVICE_UUID,
    ): DataByteArray? {
        return operationMutex.withLock {
            services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.read()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun writeCharacteristic(
        characteristicId: UUID,
        serviceId: UUID = HaarekBoardSpec.PODBIKE_SERVICE_UUID,
        value: DataByteArray
    ) {
        operationMutex.withLock {
            services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.write(value)
        }
    }

    suspend fun getCharacteristicNotifications(
        characteristicId: UUID,
        serviceId: UUID = HaarekBoardSpec.PODBIKE_SERVICE_UUID,
    ): Flow<DataByteArray>? {
        return operationMutex.withLock {
            services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.getNotifications()
        }
    }
}
