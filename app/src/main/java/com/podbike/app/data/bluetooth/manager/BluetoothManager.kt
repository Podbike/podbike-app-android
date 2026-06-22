/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.podbike.app.data.bluetooth.manager

import android.os.ParcelUuid
import com.podbike.app.data.api.model.OtaFile
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.values.HaarekBoardSpec
import com.podbike.app.ui.scanning.DeviceInfo
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

    fun transferFileToDevice(file: OtaFile): Flow<FirmwareFileTransferStatus>?

    var selectedDevice: PodbikeDevice?
}