package com.podbike.app.data.bluetooth.manager

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.podbike.app.data.api.model.FirmwareFile
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.utils.YModem
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator


class BluetoothManagerImpl(val context: Context) : BluetoothManager {
    private val connectedDevices = mutableListOf<PodbikeDevice>()
    override var selectedDevice: PodbikeDevice? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(
        device: DeviceInfo,
        waitForPairing: Boolean,
        coroutineScope: CoroutineScope,
    ): PodbikeDevice {
        try {
            val connection = ClientBleGatt.connect(
                context, device.address, coroutineScope, options = BleGattConnectOptions(
                )
            )
            println("Connecting to device: ${device.address}")

            if (waitForPairing) {
                connection.waitForBonding()
                println("Waiting for bonding with device: ${device.address}")
            }

            val podbikeDevice = PodbikeDevice(connection, device)
            connectedDevices.add(podbikeDevice)

            podbikeDevice.discoverServices()
            println("Services discovered for device: ${device.address}")

            podbikeDevice.data.initDeviceMetadata()
            println("Device metadata initialized for device: ${device.address}")

            selectedDevice = podbikeDevice
            return podbikeDevice
        } catch (e: Exception) {
            println("Failed to connect to device: ${device.address}, error: ${e.message}")
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

    override suspend fun transferFileToDevice(file: FirmwareFile) {
        selectedDevice?.let { YModem.sendFileToDevice(context, file, it) }
    }
}
