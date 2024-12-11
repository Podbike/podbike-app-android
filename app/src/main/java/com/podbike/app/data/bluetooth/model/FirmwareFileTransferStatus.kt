package com.podbike.app.data.bluetooth.model

data class FirmwareFileTransferStatus(
    val currentPackage: Int,
    val totalPackages: Int,
    val isTransferComplete: Boolean
)