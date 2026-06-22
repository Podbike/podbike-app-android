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
    val serialNumber: Int? = null,
    @SerialName("firmware_version")
    val firmwareVersion: String,
    @SerialName("file_name")
    val fileName: String,
    @SerialName("date_created")
    val dateCreated: String,
    @SerialName("date_modified")
    val dateModified: String
)