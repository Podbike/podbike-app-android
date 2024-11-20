package com.podbike.app.data.bluetooth.wrapper

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

    private suspend fun readCharacteristic(
        serviceId: UUID,
        characteristicId: UUID
    ): DataByteArray? {
        try {
            return services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.read()

        } catch (e: SecurityException) {
            println("Error reading characteristic: $e")
            return null
        }
    }

    private suspend fun writeCharacteristic(
        serviceId: UUID,
        characteristicId: UUID,
        value: DataByteArray
    ) {
        try {
            services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.write(value)
        } catch (e: SecurityException) {
            println("Error writing characteristic: $e")
        }
    }

    private suspend fun getCharacteristicNotifications(
        serviceId: UUID,
        characteristicId: UUID
    ): Flow<DataByteArray>? {
        try {
            return services?.findService(serviceId)
                ?.findCharacteristic(characteristicId)
                ?.getNotifications()

        } catch (e: SecurityException) {
            println("Error enabling notifications: $e")
            return null
        }
    }
}
