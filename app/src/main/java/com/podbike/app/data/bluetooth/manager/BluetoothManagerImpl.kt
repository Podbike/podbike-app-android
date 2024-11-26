package com.podbike.app.data.bluetooth.manager

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.wrapper.PodbikeBluetoothDeviceWrapper
import com.podbike.app.ui.dashboard.DeviceStatus
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import kotlin.random.Random

class BluetoothManagerImpl(val context: Context) : BluetoothManager {
    private val connectedDevices = mutableListOf<PodbikeBluetoothDeviceWrapper>()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(
        device: DeviceInfo,
        waitForPairing: Boolean,
        viewModelScope: CoroutineScope,
    ): PodbikeBluetoothDeviceWrapper {
        val connection = ClientBleGatt.connect(context, device.address, viewModelScope)

        if (waitForPairing)
            connection.waitForBonding()

        val podbikeDevice = PodbikeBluetoothDeviceWrapper(connection)
        connectedDevices.add(podbikeDevice)

        podbikeDevice.discoverServices()
        return podbikeDevice
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun scan(filters: List<BleScanFilter>): Flow<List<DeviceInfo>> {
        val aggregator = BleScanResultAggregator()

        return BleScanner(context).scan(filters)
            .map { aggregator.aggregateDevices(it) }
            .map { it -> it.map { DeviceInfo(it.name ?: "Unknown", it.address) } }
    }

    override fun streamDeviceStatus(): Flow<DeviceStatus> = flow {
        while (true) {
            val status = DeviceStatus(
                Random.nextInt(0, 100),
                Random.nextInt(0, 100),
                Random.nextInt(0, 100),
                Random.nextInt(0, 100)
            )
            emit(status)
            delay(2000)
        }
    }
}
