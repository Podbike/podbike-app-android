package com.podbike.app.data.bluetooth.model

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.utils.YModem
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import com.podbike.app.ui.base.collectWithErrorHandling
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.toDisplayString
import java.util.UUID
import kotlin.time.TimeMark
import kotlin.time.TimeSource

data class PodbikeDeviceData(private val device: PodbikeDevice, val deviceInfo: DeviceInfo) {

    var deviceMetadata: PodbikeDeviceMetadata? = null

    // start counting time when speed is greater than 0
    var tripStart: TimeMark? = null
    var tripStartTimeOffset: TimeMark? = null
    var tripInactivityStartTime: TimeMark? = null
    private var tripStartDistance: Float? = null
    private var clearTripDataTimerJob: Job? = null

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val speed: Flow<Int>
        get() = getStringCharacteristicData<Int>(
            HaarekBoardSpec.SPEED_CHARACTERISTIC_UUID,
            onValue = { speed ->
                if (speed > 0 && tripStart == null) {
                    val timeSource = TimeSource.Monotonic
                    tripStart = timeSource.markNow()
                    tripStartTimeOffset = timeSource.markNow()
                }
                if (speed > _maxSpeed.value) {
                    _maxSpeed.update { speed }
                }
            })

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val battery: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.BATTERY_SOC_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val distance: Flow<Float>
        get() = getStringCharacteristicData(
            HaarekBoardSpec.DISTANCE_CHARACTERISTIC_UUID,
            onValue = { distance ->
                lastDistanceValue = distance
                if (tripStartDistance == null && distance >= 0) {
                    tripStartDistance = distance
                }
            })

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
    val temperature: Flow<Float>
        get() = getStringCharacteristicData(HaarekBoardSpec.TEMPERATURE_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val averageSpeed: Flow<Float>
        get() = getStringCharacteristicData<Float>(
            HaarekBoardSpec.AVERAGE_SPEED_CHARACTERISTIC_UUID,
        )

    private var _maxSpeed = MutableStateFlow(0)
    val maxSpeed = _maxSpeed.asStateFlow()

    private var lastDistanceValue: Float = 0f
    val averageTripSpeed: Float
        get() = if (tripStart == null) 0f else
            ((lastDistanceValue - (tripStartDistance
                ?: 0f)) / tripStart!!.elapsedNow().inWholeSeconds) * 3.6f

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val averageRpm: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.AVERAGE_CADENCE_CHARACTERISTIC_UUID)

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val powerGenerated: Flow<Int>
        get() = getStringCharacteristicData(HaarekBoardSpec.GENERATED_POWER_CHARACTERISTIC_UUID)

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun initDeviceMetadata() {
        try {
            deviceMetadata = YModem.getMockedDeviceMetadata(device)
        } catch (e: Exception) {
            println("Failed to get device metadata: ${deviceInfo.address}, error: ${e.message}")
        }
    }

    private var lastLightStatus: PodbikeLightStatus = PodbikeLightStatus()

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @OptIn(FlowPreview::class)
    val lightStatus: Flow<PodbikeLightStatus>
        get() = flow {
            coroutineScope {
                device.getCharacteristicNotifications(
                    HaarekBoardSpec.LIGHT_STATUS_CHARACTERISTIC_UUID
                )?.onStart {
                    try {
                        device.readCharacteristic(HaarekBoardSpec.LIGHT_STATUS_CHARACTERISTIC_UUID)
                            ?.let { this.emit(it) }
                    } catch (e: Exception) {
                        println("Initial lights characteristic read error: $e")
                    }
                }
                    ?.collectWithErrorHandling { data ->
                        val bytes = data.value
                        println(bytes.toDisplayString())
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
                        lastLightStatus = status
                    }
            }
        }.debounce { data ->
            if ((!data.indicatorLeft && lastLightStatus.indicatorLeft) || (!data.indicatorRight && lastLightStatus.indicatorRight)) {
                600L
            } else {
                0L
            }
        }

    fun startCleanTripDataTimer() {
        if (clearTripDataTimerJob?.isActive == true) {
            return
        }
        clearTripDataTimerJob = CoroutineScope(Dispatchers.Default).launch {
            delay(5 * 60 * 1000)
            cleanTripData()
        }
    }

    fun cancelCleanTripDataTimer() {
        clearTripDataTimerJob?.cancel()
    }

    private fun cleanTripData() {
        tripStart = null
        tripStartTimeOffset = null
        tripInactivityStartTime = null
        _maxSpeed.update { 0 }
        tripStartDistance = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private inline fun <reified T> getStringCharacteristicData(
        characteristicId: UUID,
        crossinline onValue: (T) -> Unit = {},
    ): Flow<T> =
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
                    onValue(str.convertToType())
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
