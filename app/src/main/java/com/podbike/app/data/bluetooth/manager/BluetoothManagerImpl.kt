package com.podbike.app.data.bluetooth.manager

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.wrapper.PodbikeDevice
import com.podbike.app.ui.dashboard.DeviceDataUiModel
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import kotlin.random.Random

class BluetoothManagerImpl(val context: Context) : BluetoothManager {
    private val connectedDevices = mutableListOf<PodbikeDevice>()
    override var selectedDevice: PodbikeDevice? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(
        device: DeviceInfo,
        waitForPairing: Boolean,
        viewModelScope: CoroutineScope,
    ): PodbikeDevice {
        val connection = ClientBleGatt.connect(context, device.address, viewModelScope)

        if (waitForPairing)
            connection.waitForBonding()

        val podbikeDevice = PodbikeDevice(connection)
        connectedDevices.add(podbikeDevice)

        podbikeDevice.discoverServices()
        selectedDevice = podbikeDevice
        return podbikeDevice
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun scan(filters: List<BleScanFilter>): Flow<List<DeviceInfo>> {
        val aggregator = BleScanResultAggregator()

        return BleScanner(context).scan(filters)
            .map { aggregator.aggregateDevices(it) }
            .map { it -> it.map { DeviceInfo(it.name ?: "Unknown", it.address) } }
    }
}
