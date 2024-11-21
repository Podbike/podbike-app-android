package com.podbike.app.data.bluetooth.manager

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.wrapper.PodbikeBluetoothDeviceWrapper
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class BluetoothManagerImpl(val context: Context) : BluetoothManager {
    private val connectedDevices = mutableListOf<PodbikeBluetoothDeviceWrapper>()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(device: ServerDevice): PodbikeBluetoothDeviceWrapper {
            return coroutineScope {
                val connection = ClientBleGatt.connect(context, device, this)

                val podbikeDevice = PodbikeBluetoothDeviceWrapper(connection)
                connectedDevices.add(podbikeDevice)

                return@coroutineScope podbikeDevice.apply { discoverServices() }
            }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun scan(filters: List<BleScanFilter>): Flow<List<DeviceInfo>> {
            val aggregator = BleScanResultAggregator()

           return BleScanner(context).scan(filters)
                .map { aggregator.aggregateDevices(it) }
                .map { it -> it.map { DeviceInfo(it.name ?: "Unknown", it.address) } }
    }
}
