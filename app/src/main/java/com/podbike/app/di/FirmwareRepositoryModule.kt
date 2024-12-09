package com.podbike.app.di

import android.content.Context
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.manager.BluetoothManagerImpl
import com.podbike.app.data.repository.FirmwareRepository
import com.podbike.app.data.repository.FirmwareRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirmwareRepositoryModule {

    @Provides
    @Singleton
    fun provideFirmwareRepository(@ApplicationContext context: Context): FirmwareRepository {
        return FirmwareRepositoryImpl()
    }
}
