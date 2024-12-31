package com.podbike.app.data.bluetooth.model

import com.google.gson.annotations.SerializedName

data class PodbikeTransferConfig(
    @SerializedName("Product name") val productName: String,
    @SerializedName("ReleaseID") val releaseId: String,
    @SerializedName("ProductID") val productId: String,
    @SerializedName("SupportedBoards") val supportedBoards: List<SupportedBoard>,
    @SerializedName("AudioFiles") val audioFiles: List<AudioFile>
)

data class SupportedBoard(
    @SerializedName("Board name") val boardName: String,
    @SerializedName("BoardID") val boardId: String,
    @SerializedName("Serial number") val serialNumber: String,
    @SerializedName("FWVersion") val firmwareVersion: String,
    @SerializedName("Filename") val fileName: String
)