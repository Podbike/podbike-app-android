package com.podbike.app.ui.dashboard

data class DeviceDataUiModel(
    val speed: Int,
    val battery: Int,
    val distance: Float,
    val time: Int
) {
    val speedString: String
        get() = "$speed"
    val batteryString: String
        get() = "$battery"
    val distanceString: String
        get() = "$distance km"
}