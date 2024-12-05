package com.podbike.app.ui.dashboard

import com.podbike.app.data.bluetooth.model.PodbikeLightStatus

data class DeviceDataUiModel(
    val speed: String,
    val battery: Int,
    val distance: String,
    val time: Int,
    val cadence: Int,
    val assist: Int,
    val isFreezing: Boolean,
    val lightStatus: PodbikeLightStatus,
    val distanceAbbreviation: String,
    val range: String,
    val temperature: Int,
    val isMoving: Boolean,
    val name: String?
)