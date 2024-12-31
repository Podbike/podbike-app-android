package com.podbike.app.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFirmwareFilesResponse(
    @SerialName("resource")
    val data: List<FirmwareFilesData>
)

@Serializable
data class FirmwareFilesData(
    @SerialName("id")
    val id: Int,
    @SerialName("product_id")
    val productId: String,
    @SerialName("release_id")
    val releaseId: String,
    @SerialName("license_link")
    val licenseLink: String,
    @SerialName("date_created")
    val dateCreated: String,
    @SerialName("date_modified")
    val dateModified: String,
    @SerialName("firmware_module_by_update_id")
    val firmwareModuleByUpdateId: List<FirmwareModuleData>
)

@Serializable
data class FirmwareModuleData(
    @SerialName("id")
    val id: Int,
    @SerialName("update_id")
    val updateId: Int,
    @SerialName("file_index_id")
    val fileIndexId: Int,
    @SerialName("board_name")
    val boardName: String,
    @SerialName("serial_number")
    val serialNumber: String? = null,
    @SerialName("firmware_version")
    val firmwareVersion: String,
    @SerialName("file_name")
    val fileName: String,
    @SerialName("date_created")
    val dateCreated: String,
    @SerialName("date_modified")
    val dateModified: String
)