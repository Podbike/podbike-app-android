package com.podbike.app.data.repository

import com.podbike.app.data.api.model.FirmwareFile
import com.podbike.app.data.api.model.FirmwareFilesData

interface FirmwareRepository {
    suspend fun checkIsUpToDate(frameNumber: String): Boolean
    suspend fun getFirmwareFilesList(frameNumber: String): FirmwareFilesData?
    suspend fun getLicence(): String
    suspend fun getFirmwareFile(fileName: String): FirmwareFile?
}
