package com.podbike.app.data.bluetooth.manager

import android.bluetooth.BluetoothDevice
import com.podbike.app.data.bluetooth.wrapper.PodbikeBluetoothDeviceWrapper
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.kotlin.ble.core.RealServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter

interface BluetoothManager {
    suspend fun connect(device: RealServerDevice) : PodbikeBluetoothDeviceWrapper?
    fun getBluetoothDevice(name: String, address: String) : BluetoothDevice?
    fun scan(filters: List<BleScanFilter> = emptyList()) : Flow<List<DeviceInfo>>
}