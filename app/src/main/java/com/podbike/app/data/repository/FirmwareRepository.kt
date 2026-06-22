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
