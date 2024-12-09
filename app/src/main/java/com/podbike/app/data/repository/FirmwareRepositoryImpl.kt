package com.podbike.app.data.repository

import com.podbike.app.data.api.FirmwareApi
import com.podbike.app.data.api.model.FirmwareFile
import com.podbike.app.data.api.model.FirmwareFilesData
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FirmwareRepositoryImpl : FirmwareRepository {
    private val baseUrl = "https://otaprod.podbike.com/app/"

    private val firmwareApi: FirmwareApi

    init {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(interceptor = mHttpLoggingInterceptor)
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                Json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()
                )
            )
            .client(mOkHttpClient)
            .build()

        firmwareApi = retrofit.create(FirmwareApi::class.java)
    }


    override suspend fun checkIsUpToDate(frameNumber: String): Boolean {
        return try {
            val frameNumberMap = mapOf("frameNumber" to frameNumber)
            val response = firmwareApi.getUpdateStatus(frameNumberMap).awaitResponse()
            if (response.isSuccessful) {
                val responseStatus = response.body() ?: 1
                responseStatus == 1
            } else {
                false
            }
        } catch (exception: Exception) {
            println("Failed to check firmware update status: ${exception.message}")
            false
        }
    }

    override suspend fun getFirmwareFilesList(frameNumber: String): FirmwareFilesData? {
        return try {
            val response = firmwareApi.getFirmwareFilesList(frameNumber).awaitResponse()
            if (response.isSuccessful) {
                response.body()?.data?.firstOrNull()
            } else {
                null
            }
        } catch (exception: Exception) {
            println("Failed to get firmware files list: ${exception.message}")
            null
        }
    }

    override suspend fun getLicence(): String {
        return try {
            val response = firmwareApi.getLicence().awaitResponse()
            if (response.isSuccessful) {
                response.body() ?: ""
            } else {
                ""
            }
        } catch (exception: Exception) {
            println("Failed to get licence: ${exception.message}")
            ""
        }
    }

    override suspend fun getFirmwareFile(fileName: String): FirmwareFile? {
        return try {
            val response = firmwareApi.getFirmwareFile(fileName).awaitResponse()
            if (response.isSuccessful) {
                val bytes = response.body()?.byteStream()?.readBytes()
                bytes?.let {
                    FirmwareFile(fileName, it)
                }
            } else {
                null
            }
        } catch (exception: Exception) {
            println("Failed to get firmware file: ${exception.message}")
            null
        }
    }
}
