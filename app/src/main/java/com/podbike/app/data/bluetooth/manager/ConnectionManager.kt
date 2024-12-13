package com.podbike.app.data.bluetooth.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

object ConnectionManager {
    private var connectionJob = Job()
    var connectionScope = CoroutineScope(Dispatchers.IO + connectionJob)

    fun cancelConnection() {
        connectionJob.cancel()
    }
}