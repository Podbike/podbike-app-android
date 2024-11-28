package com.podbike.app.data.bluetooth.model

data class PodbikeLightStatus(
    val lowBeam: Boolean = false,
    val highBeam: Boolean = false,
    val rearLight: Boolean = false,
    val brakeLight: Boolean = false,
    val indicatorLeft: Boolean = false,
    val indicatorRight: Boolean = false,
    val reverseLight: Boolean = false,
    val runningLight: Boolean = false
)
