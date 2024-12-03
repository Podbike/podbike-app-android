package com.podbike.app.di

import android.content.Context
import com.podbike.app.ui.navigation.IntentManager
import com.podbike.app.utils.UnitConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideIntentManager(@ApplicationContext context: Context): IntentManager {
        return IntentManager(context)
    }

    @Provides
    @Singleton
    fun provideDistanceValueConverter(): UnitConverter {
        return UnitConverter()
    }
}