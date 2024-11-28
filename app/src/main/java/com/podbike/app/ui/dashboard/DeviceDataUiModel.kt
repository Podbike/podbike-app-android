package com.podbike.app.ui.dashboard

import com.podbike.app.data.bluetooth.model.PodbikeLightStatus

data class DeviceDataUiModel(
    val speed: Int,
    val battery: Int,
    val distance: Float,
    val time: Int,
    val cadence: Int,
    val assist: Int,
    val lightStatus: PodbikeLightStatus
) {
    val speedString: String
        get() = "$speed"
    val batteryString: String
        get() = "$battery"
    val distanceString: String
        get() = "${distance / 1000} km"
}