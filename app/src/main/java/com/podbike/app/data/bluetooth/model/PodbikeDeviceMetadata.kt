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
)

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
