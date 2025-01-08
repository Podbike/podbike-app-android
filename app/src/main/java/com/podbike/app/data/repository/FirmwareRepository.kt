package com.podbike.app.data.repository

import com.podbike.app.data.api.model.FirmwareFilesData
import com.podbike.app.data.api.model.OtaFile
import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import com.podbike.app.utils.DataResult

interface FirmwareRepository {
    suspend fun checkIsUpToDate(deviceMetadata: PodbikeDeviceMetadata): DataResult<Boolean>
    suspend fun getFirmwareFilesList(frameNumber: String): DataResult<FirmwareFilesData?>
    suspend fun getLicense(): DataResult<String>
    suspend fun getFirmwareFile(fileName: String): DataResult<OtaFile?>
}
