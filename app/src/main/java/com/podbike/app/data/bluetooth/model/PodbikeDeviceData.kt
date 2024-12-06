package com.podbike.app.data.bluetooth.model

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import com.podbike.app.ui.base.collectWithErrorHandling
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import java.util.UUID

data class PodbikeDeviceData(private val device: PodbikeDevice, val deviceInfo: DeviceInfo) {
    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val speed: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.SPEED_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val battery: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.BATTERY_SOC_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val distance: Flow<Float>
        get() = getStringCharacteristicData(HaarekBoardSpec.DISTANCE_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val cadence: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.CADENCE_SETTING_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val assist: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.ASSIST_SETTING_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val range: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.RANGE_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val temperature: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.TEMPERATURE_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val averageSpeed: Flow<Float>
        get() = getStringCharacteristicData(HaarekBoardSpec.AVERAGE_SPEED_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val averageRpm: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.AVERAGE_CADENCE_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val powerGenerated: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.GENERATED_POWER_CHARACTERISTIC_UUID)


    val lightStatus: Flow<PodbikeLightStatus>
        get() = flow {
            device.getCharacteristicNotifications(
                HaarekBoardSpec.LIGHT_STATUS_CHARACTERISTIC_UUID
            )?.collectWithErrorHandling { data ->
                val bytes = data.value
                print(bytes[0].toInt().toChar())
                val status = PodbikeLightStatus(
                    lowBeam = bytes[0].toInt().toChar() != '0',
                    highBeam = bytes[6].toInt().toChar() != '0',
                    rearLight = bytes[5].toInt().toChar() != '0',
                    brakeLight = bytes[4].toInt().toChar() != '0',
                    indicatorLeft = bytes[3].toInt().toChar() != '0',
                    indicatorRight = bytes[2].toInt().toChar() != '0',
                    reverseLight = bytes[1].toInt().toChar() != '0',
                    runningLight = bytes[7].toInt().toChar() != '0'
                )
                emit(status)
            }
        }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private inline fun <reified T> getStringCharacteristicData(characteristicId: UUID): Flow<T> =
        flow {
            try {
                device.readCharacteristic(characteristicId)?.let { data ->
                    val str = data.asString()
                    emit(str.convertToType())
                }
            } catch (e: Exception) {
                println("Initial characteristic read error $characteristicId: $e")
            }

            try {
                device.getCharacteristicNotifications(characteristicId)?.collect { data ->
                    val str = data.asString()
                    emit(str.convertToType())
                }
            } catch (e: Exception) {
                println("Error subscribing to characteristic $characteristicId: $e")
            }
        }

    private fun DataByteArray.asString(): String {
        val bytes = this.value.filter { it != 0.toByte() }.toByteArray()
        return bytes.toString(Charsets.UTF_8).trim()
    }

    private inline fun <reified T> String.convertToType(): T {
        return when (T::class) {
            Int::class -> this.toInt() as T
            Float::class -> this.toFloat() as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

}
