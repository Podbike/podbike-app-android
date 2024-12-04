package com.podbike.app.ui.scanning

import java.io.Serializable

data class DeviceItem(
    val name: String,
    val address: String,
    val isCurrentlyConnected: Boolean
) : Serializable

fun DeviceItem.toDeviceInfo(): DeviceInfo {
    return DeviceInfo(name, address)
}

fun DeviceInfo.toDeviceItem(isCurrentlyConnected: Boolean): DeviceItem {
    return DeviceItem(name, address, isCurrentlyConnected)
}