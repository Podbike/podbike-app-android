package com.podbike.app.services

import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class BluetoothManager {

    fun getBluetoothDevices(): Flow<DeviceInfo> = flow {
        var deviceNumber = 1
        while (true) {
            val device = DeviceInfo("Device $deviceNumber", UUID.randomUUID().toString())
            emit(device)
            deviceNumber++
            delay(5000)
        }
    }
}