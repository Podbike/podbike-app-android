package com.podbike.app.ui.scanning

import java.io.Serializable

data class DeviceInfo(
    val name: String,
    val address: String,
    val updateStarted: Boolean? = false,
    val updateConfigHash: Int? = null
): Serializable