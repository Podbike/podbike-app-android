package com.podbike.app.data.bluetooth.manager

import android.content.Context
import com.podbike.app.data.bluetooth.wrapper.PodbikeBluetoothDeviceWrapper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class BluetoothManagerImpl(val context: Context) : BluetoothManager {
    private val connectedDevices = mutableListOf<PodbikeBluetoothDeviceWrapper>()

    override suspend fun connect(device: ServerDevice): PodbikeBluetoothDeviceWrapper? {
        try {
            return coroutineScope {
                val connection = ClientBleGatt.connect(context, device, this)

                val podbikeDevice = PodbikeBluetoothDeviceWrapper(connection)
                connectedDevices.add(podbikeDevice)

                return@coroutineScope podbikeDevice.apply { discoverServices() }
            }
        } catch (e: SecurityException) {
            println("No permission to scan for bluetooth devices")
            return null
        }
    }

    override fun scan(filters: List<BleScanFilter>?): List<ServerDevice> {
        try {
            var devices: List<ServerDevice> = emptyList()
            val aggregator = BleScanResultAggregator()
            BleScanner(context).scan(filters ?: emptyList())
                .map { aggregator.aggregateDevices(it) }
                .onEach { devices = it }

            return devices
        } catch (e: SecurityException) {
            println("No permission to scan for bluetooth devices")
            return emptyList()
        }

    }
}
