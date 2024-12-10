package com.podbike.app.data.bluetooth.utils

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.bluetooth.model.AudioFile
import com.podbike.app.data.bluetooth.model.EcuModule
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

// YModem related consts
const val SOH = 0x01
const val STX = 0x02
const val EOT = 0x04
const val ACK = 0x06
const val NACK = 0x15
const val CA = 0x18
const val RQS_PKT = 0x43
const val RB = 0x72
const val CRC_BYTES_COUNT = 2

// Podbike related consts
const val PODBIKE_DTA_BYTE = 0x52
const val PODBIKE_ATD_BYTE = 0x57

class YModem {
    companion object YModemHelper {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        suspend fun getDeviceMetadata(device: PodbikeDevice): PodbikeDeviceMetadata {

            val dataFlow =
                device.getCharacteristicNotifications(HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID)


            val ackArray = byteArrayOf(PODBIKE_DTA_BYTE.toByte(), ACK.toByte())
            val rqsPktArray = byteArrayOf(PODBIKE_DTA_BYTE.toByte(), RQS_PKT.toByte())

            device.writeCharacteristic(
                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                value = DataByteArray(value = ackArray)
            )

            device.writeCharacteristic(
                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                value = DataByteArray(value = rqsPktArray)
            )

            device.writeCharacteristic(
                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                value = DataByteArray(value = rqsPktArray)
            )

            val collectedData = mutableListOf<ByteArray>()
            var fileName = ""
            var packageIndex = 0

            dataFlow
                ?.takeWhile { data -> data.value[0] != EOT.toByte() }
                ?.collect { data ->
                    val cleanedData = data.value.copyOfRange(
                        3,
                        data.size - CRC_BYTES_COUNT
                    ).filter { it != 0x00.toByte() }.toByteArray()

                    if (packageIndex == 0) {
                        fileName = cleanedData.toString(Charsets.UTF_8)
                    } else {
                        collectedData.add(cleanedData)
                    }
                    packageIndex++

                    device.writeCharacteristic(
                        HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                        value = DataByteArray(value = ackArray)
                    )

                    device.writeCharacteristic(
                        HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                        value = DataByteArray(value = ackArray)
                    )
                }

            val data = collectedData.joinToString(separator = "") { it.toString(Charsets.UTF_8) }
            val metadata = Json.decodeFromString<PodbikeDeviceMetadata>(data)
            return metadata.copy(fileName = fileName)
        }

        // Mocked data for testing purposes
        suspend fun getMockedDeviceMetadata(device: PodbikeDevice): PodbikeDeviceMetadata {
            return PodbikeDeviceMetadata(
                fileName = "device_metadata.json",
                productName = "Podbike Frikar",
                releaseId = "1.0.0",
                productId = "FRK-2024",
                frameNumber = "FRK123456789",
                ecuModules = listOf(
                    EcuModule(
                        boardName = "Main Control Board",
                        boardId = "MCB-001",
                        serialNumber = "SN123456789",
                        boardPosition = "Front",
                        fwVersion = "v1.2.3"
                    ),
                    EcuModule(
                        boardName = "Battery Management System",
                        boardId = "BMS-002",
                        serialNumber = "SN987654321",
                        boardPosition = "Rear",
                        fwVersion = "v2.3.4"
                    ),
                    EcuModule(
                        boardName = "Motor Controller",
                        boardId = "MTC-003",
                        serialNumber = "SN456789123",
                        boardPosition = "Rear",
                        fwVersion = "v3.4.5"
                    ),
                    EcuModule(
                        boardName = "Display Controller",
                        boardId = "DC-004",
                        serialNumber = "SN789123456",
                        boardPosition = "Front",
                        fwVersion = "v4.5.6"
                    )
                ),
                audioFiles = listOf(
                    AudioFile(filename = "startup_sound.mp3"),
                    AudioFile(filename = "shutdown_sound.mp3")
                )
            )
        }
    }
}

