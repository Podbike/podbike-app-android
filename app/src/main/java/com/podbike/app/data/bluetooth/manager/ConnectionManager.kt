package com.podbike.app.data.bluetooth.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object ConnectionManager {
    private val connectionJob = Job()
    val connectionScope = CoroutineScope(Dispatchers.IO + connectionJob)

    fun cancelConnection() {
        connectionJob.cancel()
    }
}