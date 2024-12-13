package com.podbike.app.data.bluetooth.manager

import android.os.ParcelUuid
import com.podbike.app.data.api.model.FirmwareFile
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import com.podbike.app.ui.scanning.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.FilteredServiceUuid

interface BluetoothManager {
    suspend fun connect(
        device: DeviceInfo,
        waitForPairing: Boolean = true,
    ): PodbikeDevice?

    fun scan(
        filters: List<BleScanFilter> =
            listOf(
                BleScanFilter(

                    serviceUuid = FilteredServiceUuid(ParcelUuid(HaarekBoardSpec.PODBIKE_SERVICE_UUID)),
                )
            )
    ): Flow<List<DeviceInfo>>


    fun disconnectAll()

    fun transferFileToDevice(file: FirmwareFile): Flow<FirmwareFileTransferStatus>?

    var selectedDevice: PodbikeDevice?
}