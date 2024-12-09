package com.podbike.app.data.repository

import com.podbike.app.data.api.FirmwareApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FirmwareRepositoryImpl : FirmwareRepository {
    private val baseUrl = "https://api.podbike.com/api/"

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
            val response = firmwareApi.getUpdateStatus(frameNumber).awaitResponse()
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

    override suspend fun getFirmwareFilesList(frameNumber: String): List<String> {
        return try {
            val response = firmwareApi.getFirmwareFilesList(frameNumber).awaitResponse()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (exception: Exception) {
            println("Failed to get firmware files list: ${exception.message}")
            emptyList()
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

    override suspend fun getFirmwareFile(frameNumber: String, fileName: String): ByteArray? {
        return try {
            val response = firmwareApi.getFirmwareFile(fileName).awaitResponse()
            if (response.isSuccessful) {
                response.body() ?: ByteArray(0)
            } else {
                ByteArray(0)
            }
        } catch (exception: Exception) {
            println("Failed to get firmware file: ${exception.message}")
            ByteArray(0)
        }
    }
}
