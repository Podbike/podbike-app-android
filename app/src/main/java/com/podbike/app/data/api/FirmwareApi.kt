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
