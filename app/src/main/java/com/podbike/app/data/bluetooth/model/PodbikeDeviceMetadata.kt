package com.podbike.app.data.bluetooth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PodbikeDeviceMetadata(
    val fileName: String? = null,
    @SerialName("Product name")
    val productName: String,
    @SerialName("ReleaseID")
    val releaseId: String,
    @SerialName("ProductID")
    val productId: String,
    @SerialName("Frame number")
    val frameNumber: String,
    @SerialName("ECU Modules")
    val ecuModules: List<EcuModule>,
    @SerialName("AudioFiles")
    val audioFiles: List<AudioFile>
) {
    companion object {
        fun mock(): PodbikeDeviceMetadata = PodbikeDeviceMetadata(
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
}

@Serializable
data class EcuModule(
    @SerialName("Board name")
    val boardName: String,
    @SerialName("BoardID")
    val boardId: String,
    @SerialName("Serial number")
    val serialNumber: String,
    @SerialName("Board position")
    val boardPosition: String,
    @SerialName("FWVersion")
    val fwVersion: String
)

@Serializable
data class AudioFile(
    @SerialName("Filename")
    val filename: String
)
