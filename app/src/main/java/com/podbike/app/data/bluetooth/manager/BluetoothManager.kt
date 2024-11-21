package com.podbike.app.data.bluetooth.manager

import com.podbike.app.data.bluetooth.wrapper.PodbikeBluetoothDeviceWrapper
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter

interface BluetoothManager {
    suspend fun connect(device: ServerDevice) : PodbikeBluetoothDeviceWrapper?
    fun scan(filters: List<BleScanFilter>?) : List<ServerDevice>
}