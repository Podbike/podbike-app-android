package com.podbike.app.data.bluetooth.manager

import com.podbike.app.data.bluetooth.wrapper.PodbikeBluetoothDeviceWrapper
import com.podbike.app.ui.dashboard.DeviceStatus
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter

interface BluetoothManager {
    suspend fun connect(
        device: DeviceInfo,
        waitForPairing: Boolean = true,
        viewModelScope: CoroutineScope
    ): PodbikeBluetoothDeviceWrapper?

    fun scan(filters: List<BleScanFilter> = emptyList()): Flow<List<DeviceInfo>>
    fun streamDeviceStatus(): Flow<DeviceStatus>
}