package com.podbike.app.data.bluetooth.utils

import android.Manifest
import androidx.annotation.RequiresPermission
import com.podbike.app.data.api.model.OtaFile
import com.podbike.app.data.bluetooth.manager.ConnectionManager
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferState
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import kotlinx.serialization.json.Json
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.toDisplayString
import timber.log.Timber.Forest.e
import timber.log.Timber.Forest.i
import kotlin.time.Duration.Companion.seconds

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
const val PODBIKE_UPDATE_BYTE = 0x55

// Other
const val DEFAULT_CHUNK_SIZE = 128
val TIMEOUT_DURATION = 20.seconds

enum class GetDeviceMetadataInfo(val message: String) {
    REGISTERING_LISTENER("Registering listener for device metadata"),
    SENDING_RQS_PKT("Sending RQS_PKT to request data"),
    RECEIVED_DATA("Received data: "),
    SENDING_ACK("Sending ACK for package "),
    SENDING_NACK("Sending NACK for package "),
    DELAY_BEFORE_RQS_PKT("300ms delay before sending RQS_PKT"),
    SENDING_RQS_PKT_AGAIN("Sending RQS_PKT to request data"),
    SENDING_ACK_FOR_EOT("Sending ACK for EOT"),
    PARSED_METADATA("Parsed metadata: ")
}

class YModem {
    companion object YModemHelper {

        private var logMessages = mutableListOf<String>()

        @OptIn(FlowPreview::class)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        suspend fun getDeviceMetadata(device: PodbikeDevice): PodbikeDeviceMetadata? {

            if (!device.client.isConnected) return null

            val dataFlow =
                device.getCharacteristicNotifications(HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID)

            val ackArray = byteArrayOf(PODBIKE_DTA_BYTE.toByte(), ACK.toByte())
            val nackArray = byteArrayOf(PODBIKE_DTA_BYTE.toByte(), NACK.toByte())
            val rqsPktArray = byteArrayOf(PODBIKE_DTA_BYTE.toByte(), RQS_PKT.toByte())

            val collectedData = mutableListOf<ByteArray>()
            var fileName = ""
            var packageIndex = 0

            if (dataFlow == null) {
                e("Fatal error: Device metadata data flow is null")
                return null
            }

            dataFlow
                .shareIn(
                    scope = ConnectionManager.connectionScope,
                    started = SharingStarted.WhileSubscribed(),
                )
                .onSubscription {
                    logStatus(GetDeviceMetadataInfo.REGISTERING_LISTENER)
                    logStatus(GetDeviceMetadataInfo.SENDING_RQS_PKT)
                    device.writeCharacteristic(
                        HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                        value = DataByteArray(value = rqsPktArray)
                    )
                }
                .takeWhile { data -> data.value[0] != EOT.toByte() }
                .timeout(TIMEOUT_DURATION)
                .collect { data ->
                    logStatus(
                        GetDeviceMetadataInfo.RECEIVED_DATA,
                        data.value.toDisplayString()
                    )
                    val payload = data.value.copyOfRange(3, data.size - CRC_BYTES_COUNT)
                    val cleanedPayload = payload.filter { it != 0x00.toByte() }.toByteArray()

                    val receivedCrc = data.value.copyOfRange(data.size - CRC_BYTES_COUNT, data.size)
                    val expectedCrc = calculateCRC(payload)

                    if (!receivedCrc.contentEquals(expectedCrc)) {
                        logStatus(GetDeviceMetadataInfo.SENDING_NACK, packageIndex.toString())
                        device.writeCharacteristic(
                            HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                            value = DataByteArray(value = nackArray)
                        )
                        return@collect
                    }

                    if (packageIndex == 0) {
                        fileName = cleanedPayload.toString(Charsets.UTF_8)
                    } else {
                        collectedData.add(cleanedPayload)
                    }

                    logStatus(GetDeviceMetadataInfo.SENDING_ACK, packageIndex.toString())
                    device.writeCharacteristic(
                        HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                        value = DataByteArray(value = ackArray)
                    )

                    if (packageIndex == 0) {
                        logStatus(GetDeviceMetadataInfo.DELAY_BEFORE_RQS_PKT)
                        delay(300) // the device needs to receive ACK and RQS_PKT separately
                        logStatus(GetDeviceMetadataInfo.SENDING_RQS_PKT_AGAIN)
                        device.writeCharacteristic(
                            HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                            value = DataByteArray(value = rqsPktArray)
                        )
                    }

                    packageIndex++
                }

            logStatus(GetDeviceMetadataInfo.SENDING_ACK_FOR_EOT)
            device.writeCharacteristic(
                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                value = DataByteArray(value = ackArray)
            )

            val data = collectedData.joinToString(separator = "") { it.toString(Charsets.UTF_8) }
            val metadata = Json.decodeFromString<PodbikeDeviceMetadata>(data)
            logStatus(GetDeviceMetadataInfo.PARSED_METADATA, metadata.toString())
            return metadata.copy(fileName = fileName)
        }

        @OptIn(FlowPreview::class)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun sendFileToDevice(
            file: OtaFile,
            device: PodbikeDevice
        ): Flow<FirmwareFileTransferStatus> = flow {
            var operationError = false
            try {
                val dataFlow =
                    device.getCharacteristicNotifications(HaarekBoardSpec.FTP_CONTROL_CHARACTERISTIC_UUID)

                val header = prepareDataPacket(getHeaderData(file), 0)

                // split file data for chunks of 128 bytes
                val dataChunks =
                    splitByteArrayIntoChunks(file.bytes!!, 128).mapIndexed { index, data ->
                        prepareDataPacket(data, index + 1)
                    }

                emit(
                    FirmwareFileTransferStatus(
                        currentPackage = 0,
                        totalPackages = dataChunks.size,
                        status = FirmwareFileTransferState.TRANSFERRING
                    )
                )

                var packageIndex = 0
                var eotSent = false
                var transferComplete = false

                dataFlow
                    ?.shareIn(
                        scope = ConnectionManager.connectionScope,
                        started = SharingStarted.WhileSubscribed(),
                    )
                    ?.onSubscription {
                        val atdArray = byteArrayOf(PODBIKE_ATD_BYTE.toByte())
                        device.writeCharacteristic(
                            HaarekBoardSpec.FTP_CONTROL_CHARACTERISTIC_UUID,
                            value = DataByteArray(value = atdArray)
                        )
                        device.writeCharacteristic(
                            HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                            value = DataByteArray(value = header)
                        )
                    }
                    ?.takeWhile { !transferComplete && !operationError }
                    ?.timeout(TIMEOUT_DURATION)
                    ?.collect { data ->
                        i("[FirmwareUpdate] Transferring packet ${packageIndex + 1} of ${dataChunks.size}")
                        if (packageIndex < dataChunks.size && data.value[0] == ACK.toByte()) {
                            emit(
                                FirmwareFileTransferStatus(
                                    currentPackage = packageIndex + 1,
                                    totalPackages = dataChunks.size,
                                    status = FirmwareFileTransferState.TRANSFERRING
                                )
                            )
                            writeFirmwareValue(device, dataChunks[packageIndex])
                            packageIndex++
                        } else if (packageIndex == dataChunks.size && data.value[0] == ACK.toByte() && !eotSent) {
                            i("[FirmwareUpdate] All packets sent, sending EOT")
                            val eotArray = byteArrayOf(PODBIKE_ATD_BYTE.toByte(), EOT.toByte())
                            writeFirmwareValue(device, eotArray)
                            eotSent = true
                        } else if (eotSent && data.value[0] == RQS_PKT.toByte()) {
                            i("[FirmwareUpdate] Got RQS_PKT after EOT, transfer complete; sending NULL packet")
                            emit(
                                FirmwareFileTransferStatus(
                                    currentPackage = packageIndex,
                                    totalPackages = dataChunks.size,
                                    status = FirmwareFileTransferState.COMPLETED
                                )
                            )
                            transferComplete = true
                            val nullPacket = prepareDataPacket(ByteArray(128) { 0 }, 0)
                            writeFirmwareValue(device, nullPacket)
                        } else if (data.value[0] == NACK.toByte()) {
                            e("Got NAK from device; resending packet")
                            packageIndex--
                            writeFirmwareValue(device, dataChunks[packageIndex])
                        } else if (data.value[0] == CA.toByte()) {
                            throw Exception("Got CA from device; aborting transfer")
                        }
                    }

                i("[FirmwareUpdate] File transfer complete")
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) {
                    e("[FirmwareUpdate] Cancel requested")
                    val abortArray =
                        byteArrayOf(PODBIKE_ATD_BYTE.toByte(), CA.toByte(), CA.toByte())
                    runCatching {
                        device.writeCharacteristic(
                            HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                            value = DataByteArray(value = abortArray)
                        )
                    }
                } else {
                    e("[FirmwareUpdate] Failed to send file: ${file.name}, error: ${e.message}")
                    emit(
                        FirmwareFileTransferStatus(
                            currentPackage = 0,
                            totalPackages = 0,
                            status = FirmwareFileTransferState.FAILED
                        )
                    )
                    operationError = true
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        suspend fun runUpgrade(device: PodbikeDevice) {
            writeFirmwareValue(device, byteArrayOf(PODBIKE_UPDATE_BYTE.toByte()))
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private suspend fun writeFirmwareValue(device: PodbikeDevice, data: ByteArray) {
            device.writeCharacteristic(
                HaarekBoardSpec.FTP_DATA_CHARACTERISTIC_UUID,
                value = DataByteArray(value = data)
            )
        }

        private fun getHeaderData(file: OtaFile): ByteArray {
            val data = ByteArray(128) { 0 }
            val fileNameBytes = file.name.toByteArray(Charsets.UTF_8)
            val fileSizeBytes = file.bytes!!.size.toString().toByteArray(Charsets.UTF_8)

            fileNameBytes.copyInto(data, 0, 0, fileNameBytes.size)
            fileSizeBytes.copyInto(data, fileNameBytes.size + 1, 0, fileSizeBytes.size)
            i("[FirmwareUpdate] Header for file ${file.name} (as bytes): ${data.toDisplayString()}")
            return data
        }

        // 0x57 SOH 01 FE Data[128] CRC CRC
        // or if last
        // 0x57 SOH 04 FA Data[100] CRC CRC
        private fun prepareDataPacket(
            data: ByteArray,
            packageIndex: Int
        ): ByteArray {
            val packageSequence = packageIndex % 256
            var packet = ByteArray(134) { 0 }
            packet[0] = PODBIKE_ATD_BYTE.toByte()
            packet[1] = SOH.toByte()
            packet[2] = packageSequence.toByte()
            packet[3] = (255 - packageSequence).toByte()
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
            chunkSize: Int = DEFAULT_CHUNK_SIZE
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

        private fun logStatus(info: GetDeviceMetadataInfo, additionalInfo: String = "") {
            val message = info.message + additionalInfo
            logMessages.add(message)
            i(message)
        }

        fun getAndClearLogMessages(): List<String> {
            val messages = logMessages.toList()
            logMessages.clear()
            return messages
        }
    }
}