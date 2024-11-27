package com.podbike.app.data.bluetooth.wrapper

import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class PodbikeDeviceData(private val device: PodbikeDevice) {
    val speed: Flow<Int>
        get() =
            flow {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.PODBIKE_SERVICE_UUID,
                    HaarekBoardSpec.SPEED_CHARACTERISTIC_UUID
                )?.collect { data ->
                    val str = data.value.toString(Charsets.UTF_8)
                    emit(str.trim().toInt())
                }
            }

    val battery: Flow<Int>
        get() =
            flow {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.PODBIKE_SERVICE_UUID,
                    HaarekBoardSpec.BATTERY_SOC_CHARACTERISTIC_UUID
                )?.collect { data ->
                    val str = data.value.toString(Charsets.UTF_8)
                    emit(str.trim().toInt())
                }
            }

    val distance: Flow<Float>
        get() =
            flow {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.PODBIKE_SERVICE_UUID,
                    HaarekBoardSpec.DISTANCE_CHARACTERISTIC_UUID
                )?.collect { data ->
                    val str = data.value.toString(Charsets.UTF_8)
                    emit(str.trim().toFloat())
                }
            }
}
