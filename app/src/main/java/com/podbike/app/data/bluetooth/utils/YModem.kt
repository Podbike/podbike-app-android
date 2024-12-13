package com.podbike.app.data.bluetooth.utils

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.api.model.FirmwareFile
import com.podbike.app.data.bluetooth.model.AudioFile
import com.podbike.app.data.bluetooth.model.EcuModule
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferState
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.toDisplayString

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
                    println("Received data: ${data.value.toDisplayString()}")
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
                productName = "Podbike FRIKAR",
                releaseId = "1.0.0",
                productId = "FRK-2024",
                frameNumber = "000000-F8-1-00-000",
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

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun sendFileToDevice(
            file: FirmwareFile,
            device: PodbikeDevice
        ): Flow<FirmwareFileTransferStatus> = flow {
            try {
                println("Sending file: ${file.name} ${file.bytes.size}")
                val dataFlow =
                    device.getCharacteristicNotifications(HaarekBoardSpec.FTP_CONTROL_CHARACTERISTIC_UUID)

                val header = prepareHeader(file)
                println("FILE: ${file.name} header ${header.toDisplayString()}")
                // split file data for chunks of 128 bytes
                val dataChunks =
                    splitByteArrayIntoChunks(file.bytes, 128).mapIndexed { index, data ->
                        prepareDataPacket(data, index)
                    }
                //dataChunks.forEachIndexed({ index, chunk -> println("FILE: ${file.name} packet $index ${chunk.toDisplayString()}") })

                val atdArray = byteArrayOf(PODBIKE_ATD_BYTE.toByte())
                device.writeCharacteristic(
                    HaarekBoardSpec.FTP_CONTROL_CHARACTERISTIC_UUID,
                    value = DataByteArray(value = atdArray)
                )

                println("ATD sent")

                withContext(Dispatchers.IO) {
                    Thread.sleep(1000)
                }

                println("Slept")

                device.writeCharacteristic(
                    HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                    value = DataByteArray(value = header)
                )

                println("Header sent")


                var packageIndex = 0
                var eotSent = false
                var transferComplete = false

                dataFlow
                    ?.takeWhile { !transferComplete }
                    ?.collect { data ->
                        println("Received data (${packageIndex + 1} / ${dataChunks.size}): ${data.value.toDisplayString()}")
                        if (packageIndex == 0 && data.value[0] == RQS_PKT.toByte()) {
                            device.writeCharacteristic(
                                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                                value = DataByteArray(value = dataChunks[packageIndex])
                            )
                            packageIndex++
                        } else if (packageIndex >= 0 && packageIndex < dataChunks.size && data.value[0] == ACK.toByte()) {
                            emit(
                                FirmwareFileTransferStatus(
                                    currentPackage = packageIndex + 1,
                                    totalPackages = dataChunks.size,
                                    status = FirmwareFileTransferState.TRANSFERRING
                                )
                            )
                            device.writeCharacteristic(
                                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                                value = DataByteArray(value = dataChunks[packageIndex])
                            )
                            packageIndex++

                        } else if (packageIndex == dataChunks.size && data.value[0] == ACK.toByte() && !eotSent) {
                            println("ALl packets sent, sending EOT")
                            val eotArray = byteArrayOf(PODBIKE_ATD_BYTE.toByte(), EOT.toByte())
                            device.writeCharacteristic(
                                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                                value = DataByteArray(value = eotArray)
                            )
                            eotSent = true
                        } else if (eotSent && data.value[0] == RQS_PKT.toByte()) {
                            emit(
                                FirmwareFileTransferStatus(
                                    currentPackage = packageIndex,
                                    totalPackages = dataChunks.size,
                                    status = FirmwareFileTransferState.COMPLETED
                                )
                            )
                            transferComplete = true
                            val nullPacket = prepareDataPacket(ByteArray(128) { 0 }, 0)
                            device.writeCharacteristic(
                                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                                value = DataByteArray(value = nullPacket)
                            )
                        } else if (data.value[0] == NACK.toByte() || data.value[0] == CA.toByte()) {
                            println("Error sending packet, stop transfer")
                            emit(
                                FirmwareFileTransferStatus(
                                    currentPackage = packageIndex,
                                    totalPackages = dataChunks.size,
                                    status = FirmwareFileTransferState.FAILED
                                )
                            )
                        }
                    }

            } catch (e: Exception) {
                println("Failed to send file: ${file.name}, error: ${e.message}")
            }
        }

        // 0x57 SOH 00 FF foo.c FILE_SIZE NUL[123] CRC CRC
        //@TODO: MERGE WITH PREPARE DATA PACKET FUNCTION
        private fun prepareHeader(file: FirmwareFile): ByteArray {
            val header = ByteArray(134) { 0 }
            header[0] = PODBIKE_ATD_BYTE.toByte()
            header[1] = SOH.toByte()
            header[3] = 0xFF.toByte()
            val fileNameBytes = file.name.toByteArray(Charsets.UTF_8)
            fileNameBytes.copyInto(header, 4, 0, fileNameBytes.size)
            val fileSizeBytes = file.bytes.size.toString().toByteArray(Charsets.UTF_8)
            fileSizeBytes.copyInto(header, 4 + fileNameBytes.size + 1, 0, fileSizeBytes.size)
            // calculate crc for only zeros
            val crc =
                calculateCRC(header.copyOfRange(4, 132))
            crc.copyInto(header, 132, 0, CRC_BYTES_COUNT)
            return header
        }

        // 0x57 SOH 01 FE Data[128] CRC CRC
        // or if last
        // 0x57 SOH 04 FA Data[100] CRC CRC
        private fun prepareDataPacket(
            data: ByteArray,
            packageIndex: Int
        ): ByteArray {
            var packet = ByteArray(134) { 0 }
            packet[0] = PODBIKE_ATD_BYTE.toByte()
            packet[1] = SOH.toByte()
            packet[2] = packageIndex.toByte()
            packet[3] = (0xFF - packageIndex).toByte()
            data.copyInto(packet, 4, 0, data.size)
            packet = packet.copyOfRange(0, 4 + data.size + 2)
            val crc = calculateCRC(packet.copyOfRange(4, 4 + data.size))
            crc.copyInto(packet, 4 + data.size, 0, CRC_BYTES_COUNT)
            return packet
        }

        private fun calculateCRC(data: ByteArray): ByteArray {
            var crc = 0
            for (i in data.indices) {
                crc = crc xor (data[i].toInt() shl 8)
                for (j in 0 until 8) {
                    crc = if (crc and 0x8000 != 0) {
                        crc shl 1 xor 0x1021
                    } else {
                        crc shl 1
                    }
                }
            }
            return byteArrayOf((crc shr 8).toByte(), (crc and 0xFF).toByte())
        }

        private fun splitByteArrayIntoChunks(
            byteArray: ByteArray,
            chunkSize: Int = 128
        ): List<ByteArray> {
            val chunks = mutableListOf<ByteArray>()
            var start = 0
            while (start < byteArray.size) {
                val end = minOf(start + chunkSize, byteArray.size)
                val chunk = byteArray.copyOfRange(start, end)
                chunks.add(chunk)
                start += chunkSize
            }
            return chunks
        }

    }
}

