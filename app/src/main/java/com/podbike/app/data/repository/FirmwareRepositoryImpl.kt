package com.podbike.app.data.repository

import com.podbike.app.data.api.FirmwareApi
import com.podbike.app.data.api.model.FirmwareFilesData
import com.podbike.app.data.api.model.OtaFile
import com.podbike.app.data.api.model.OtaFileType
import com.podbike.app.utils.DataResult
import com.podbike.app.utils.runWithErrorHandling
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
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


    override suspend fun checkIsUpToDate(frameNumber: String): DataResult<Boolean> {
        return runWithErrorHandling {
            val frameNumberMap = mapOf("frameNumber" to frameNumber)
            firmwareApi.getUpdateStatus(frameNumberMap) == 1
        }
    }

    override suspend fun getFirmwareFilesList(frameNumber: String): DataResult<FirmwareFilesData?> {
        return runWithErrorHandling {
            firmwareApi.getFirmwareFilesList(frameNumber).data.firstOrNull()
        }
    }

    override suspend fun getLicense(): DataResult<String> {
        return runWithErrorHandling {
            firmwareApi.getLicence().string()
        }
    }

    override suspend fun getFirmwareFile(fileName: String): DataResult<OtaFile?> {
        return runWithErrorHandling {
            val response = firmwareApi.getFirmwareFile(fileName)
            val bytes = response.byteStream().readBytes()
            OtaFile(OtaFileType.FIRMWARE, fileName, bytes)
        }
    }
}
