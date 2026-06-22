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

package com.podbike.app.data.bluetooth.manager

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.podbike.app.data.api.model.OtaFile
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.utils.YModem
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import timber.log.Timber


class BluetoothManagerImpl(val context: Context) : BluetoothManager {
    private val connectedDevices = mutableListOf<PodbikeDevice>()
    override var selectedDevice: PodbikeDevice? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(
        device: DeviceInfo,
        waitForPairing: Boolean,
    ): PodbikeDevice {
        try {
            if (selectedDevice?.device?.address == device.address) {
                Timber.i("Reconnecting to device: ${device.address}")
                selectedDevice?.client?.reconnect()
                return selectedDevice!!
            }
            val connection = ClientBleGatt.connect(
                context,
                device.address,
                ConnectionManager.connectionScope,
                options = BleGattConnectOptions(
                    autoConnect = true,
                    closeOnDisconnect = false
                )
            )
            println("Connecting to device: ${device.address}")

            if (waitForPairing) {
                connection.waitForBonding()
                Timber.d("Waiting for bonding with device: ${device.address}")
            }

            val podbikeDevice = PodbikeDevice(connection, device)
            connectedDevices.add(podbikeDevice)

            podbikeDevice.data.clearInMemoryDeviceMetadata()
            podbikeDevice.discoverServices()
            Timber.d("Services discovered for device: ${device.address}")

            selectedDevice = podbikeDevice
            return podbikeDevice
        } catch (e: Exception) {
            Timber.e("Failed to connect to device: ${device.address}, error: ${e.message}")
            throw e
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun scan(filters: List<BleScanFilter>): Flow<List<DeviceInfo>> {
        val aggregator = BleScanResultAggregator()

        return BleScanner(context).scan(
            filters,
            settings = BleScannerSettings(includeStoredBondedDevices = false)
        )
            .map { aggregator.aggregateDevices(it) }
            .map { it -> it.map { DeviceInfo(it.name ?: "Unknown", it.address) } }
    }

    override fun disconnectAll() {
        for (device in connectedDevices) {
            device.client.disconnect()
        }
        connectedDevices.clear()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun transferFileToDevice(file: OtaFile): Flow<FirmwareFileTransferStatus>? =
        selectedDevice?.let { YModem.sendFileToDevice(file, it) }
}
