package com.podbike.app.data.api.model

const val FRIKAR_TRANSFER_CONFIG_FILE_NAME = "frikar.json"

enum class OtaFileType {
    FRIKAR_TRANSFER_CONFIG, // frikar.json
    FIRMWARE,
    AUDIO;

    companion object {
        fun fromFileName(fileName: String): OtaFileType {
            return when {
                fileName.endsWith(".wav") -> AUDIO
                fileName == FRIKAR_TRANSFER_CONFIG_FILE_NAME -> FRIKAR_TRANSFER_CONFIG
                else -> FIRMWARE
            }
        }
    }
}

data class OtaFile(
    val type: OtaFileType,
    val name: String,
    val bytes: ByteArray?,
)
