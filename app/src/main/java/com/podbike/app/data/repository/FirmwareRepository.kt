package com.podbike.app.data.repository

interface FirmwareRepository {
    suspend fun checkIsUpToDate(frameNumber: String): Boolean
    suspend fun getFirmwareFilesList(frameNumber: String): List<String>
    suspend fun getLicence(): String
    suspend fun getFirmwareFile(frameNumber: String, fileName: String): ByteArray?
}
