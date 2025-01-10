package com.podbike.app.data.bluetooth.model

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import timber.log.Timber
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * A Wrapper class over ClientBleGatt for easy access to the methods specific for Podbike.
 */

class PodbikeDevice(val client: ClientBleGatt, val device: DeviceInfo) {

    private var services: ClientBleGattServices? = null

    val data = PodbikeDeviceData(this, device)

    suspend fun discoverServices() {
        services = client.discoverServices()
    }

    fun isConnected(): Flow<Boolean> =
        client.connectionState.map { it == GattConnectionState.STATE_CONNECTED }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun readCharacteristic(
        characteristicId: UUID,
        serviceId: UUID = HaarekBoardSpec.PODBIKE_SERVICE_UUID,
    ): DataByteArray? {
        return services?.findService(serviceId)
            ?.findCharacteristic(characteristicId)
            ?.read()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun writeCharacteristic(
        characteristicId: UUID,
        serviceId: UUID = HaarekBoardSpec.PODBIKE_SERVICE_UUID,
        value: DataByteArray
    ) {
        services?.findService(serviceId)
            ?.findCharacteristic(characteristicId)
            ?.write(value)

    }

    suspend fun getCharacteristicNotifications(
        characteristicId: UUID,
        serviceId: UUID = HaarekBoardSpec.PODBIKE_SERVICE_UUID,
    ): Flow<DataByteArray>? {
        try {
            val value = withTimeout(3.seconds) {
                services?.findService(serviceId)
                    ?.findCharacteristic(characteristicId)
                    ?.getNotifications()
            }
            return value
        } catch (e: TimeoutCancellationException) {
            client.disconnect()
            client.reconnect()
            Timber.e(e, "getCharacteristicNotifications timeout")
            return null
        }
    }
}
