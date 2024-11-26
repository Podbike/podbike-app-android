package com.podbike.app.ui.dashboard

data class DeviceStatus(val speed: Int, val battery: Int, val distance: Int, val time: Int) {
    val speedString: String
        get() = "$speed"
    val batteryString: String
        get() = "$battery"
    val distanceString: String
        get() = "$distance km"
}