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

package com.podbike.app.data.api

import com.podbike.app.data.api.model.GetFirmwareFilesResponse
import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FirmwareApi {
    @POST("firmware_UpToDateCheck?returns=bit")
    suspend fun getUpdateStatus(@Body frameNumber: PodbikeDeviceMetadata): Int

    @GET("firmware_update")
    suspend fun getFirmwareFilesList(@Query("frameNumber") frameNumber: String): GetFirmwareFilesResponse

    @GET("licenses/get_license")
    suspend fun getLicence(): ResponseBody

    @GET("firmware/{fileName}")
    suspend fun getFirmwareFile(@Path("fileName") fileName: String): ResponseBody
}
