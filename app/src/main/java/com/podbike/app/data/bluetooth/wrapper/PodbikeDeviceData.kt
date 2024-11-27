package com.podbike.app.data.bluetooth.wrapper

import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PodbikeDeviceData(private val device: PodbikeDevice) {
    val speed: Flow<Int>
        get() =
            flow {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.PODBIKE_SERVICE_UUID,
                    HaarekBoardSpec.SPEED_CHARACTERISTIC_UUID
                )?.collect { data ->
                    emit(data.value[0].toInt())
                }
            }

    val battery: Flow<Int>
        get() =
            flow {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.PODBIKE_SERVICE_UUID,
                    HaarekBoardSpec.BATTERY_SOC_CHARACTERISTIC_UUID
                )?.collect { data ->
                    emit(data.value[0].toInt())
                }
            }

    val distance: Flow<Float>
        get() =
            flow {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.PODBIKE_SERVICE_UUID,
                    HaarekBoardSpec.DISTANCE_CHARACTERISTIC_UUID
                )?.collect { data ->
                    emit(data.value[0].toFloat())
                }
            }
}
