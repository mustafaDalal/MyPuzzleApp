package com.md.mypuzzleapp.di

import com.md.mypuzzleapp.data.remote.api.ImageApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideImageApi(okHttpClient: OkHttpClient): ImageApi {
        return Retrofit.Builder()
            .baseUrl(ImageApi.BASE_URL)
            .client(okHttpClient)
            .build()
            .create(ImageApi::class.java)
    }
} 