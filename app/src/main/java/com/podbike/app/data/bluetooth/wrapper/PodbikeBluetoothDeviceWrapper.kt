package com.podbike.app.data.bluetooth.wrapper

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import java.util.UUID

/**
 * A Wrapper class over ClientBleGatt for easy access to the methods specific for Podbike.
 */

class PodbikeBluetoothDeviceWrapper(private val client: ClientBleGatt) {

    private var services: ClientBleGattServices? = null

    suspend fun discoverServices() {
        services = client.discoverServices()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun readCharacteristic(
        serviceId: UUID,
        characteristicId: UUID
    ): DataByteArray? {
            return services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.read()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun writeCharacteristic(
        serviceId: UUID,
        characteristicId: UUID,
        value: DataByteArray
    ) {
            services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.write(value)

    }

    private suspend fun getCharacteristicNotifications(
        serviceId: UUID,
        characteristicId: UUID
    ): Flow<DataByteArray>? {
            return services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.getNotifications()
    }
}
