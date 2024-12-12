package com.podbike.app.data.bluetooth.model

data class FirmwareFileTransferStatus(
    val currentPackage: Int,
    val totalPackages: Int,
    val status: FirmwareFileTransferState
)

enum class FirmwareFileTransferState {
    TRANSFERRING,
    COMPLETED,
    FAILED
}