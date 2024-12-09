package com.podbike.app.data.api

import com.podbike.app.data.api.model.FirmwareFilesData
import com.podbike.app.data.api.model.GetFirmwareFilesResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FirmwareApi {
    @POST("/app/firmware_UpToDateCheck?returns=bit")
    fun getUpdateStatus(@Body frameNumber: Map<String, String>): Call<Int>

    @GET("/app/firmware_update")
    fun getFirmwareFilesList(@Query("frameNumber") frameNumber: String): Call<GetFirmwareFilesResponse>

    @GET("/app/licences/get_licence")
    fun getLicence(): Call<String>

    @GET("/app/firmware/{fileName}")
    fun getFirmwareFile(@Path("fileName") fileName: String): Call<ResponseBody>
}
