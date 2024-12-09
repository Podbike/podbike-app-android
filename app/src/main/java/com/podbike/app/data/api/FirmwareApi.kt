package com.podbike.app.data.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FirmwareApi {
    @GET("/app/firmware_UpToDateCheck?returns=bit")
    fun getUpdateStatus(@Body frameNumber: String): Call<Int>

    @GET("/app/firmware_update")
    fun getFirmwareFilesList(@Query("frameNumber") frameNumber: String): Call<List<String>>

    @GET("/app/licences/get_licence")
    fun getLicence(): Call<String>

    @GET("/app/firmware/{fileName}")
    fun getFirmwareFile(@Path("fileName") fileName: String): Call<ByteArray>
}
