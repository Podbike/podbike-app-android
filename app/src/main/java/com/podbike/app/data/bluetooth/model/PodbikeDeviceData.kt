package com.podbike.app.data.bluetooth.model

import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

data class PodbikeDeviceData(private val device: PodbikeDevice) {
    val speed: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.SPEED_CHARACTERISTIC_UUID)

    val battery: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.BATTERY_SOC_CHARACTERISTIC_UUID)

    val distance: Flow<Float>
        get() = getStringCharacteristicData(HaarekBoardSpec.DISTANCE_CHARACTERISTIC_UUID)

    val cadence: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.CADENCE_SETTING_CHARACTERISTIC_UUID)

    val assist: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.ASSIST_SETTING_CHARACTERISTIC_UUID)


    val lightStatus: Flow<PodbikeLightStatus>
        get() = flow {
            device.getCharacteristicNotifications(
                HaarekBoardSpec.LIGHT_STATUS_CHARACTERISTIC_UUID
            )?.collect { data ->
                val bytes = data.value
                val status = PodbikeLightStatus(
                    lowBeam = bytes[0].toInt() != 0,
                    highBeam = bytes[6].toInt() != 0,
                    rearLight = bytes[5].toInt() != 0,
                    brakeLight = bytes[4].toInt() != 0,
                    indicatorLeft = bytes[3].toInt() != 0,
                    indicatorRight = bytes[2].toInt() != 0,
                    reverseLight = bytes[1].toInt() != 0,
                    runningLight = bytes[7].toInt() != 0
                )
                emit(status)
            }
        }

    private inline fun <reified T> getStringCharacteristicData(characteristicId: UUID): Flow<T> =
        flow {
            device.getCharacteristicNotifications(characteristicId)?.collect { data ->
                val bytes = data.value.filter { it != 0.toByte() }.toByteArray()
                val str = bytes.toString(Charsets.UTF_8).trim()

                when (T::class) {
                    Int::class -> emit(str.toInt() as T)
                    Float::class -> emit(str.toFloat() as T)
                }
            }
        }
}
