package com.md.mypuzzleapp.data.remote.datasource

import android.content.Context
import com.md.mypuzzleapp.data.remote.api.ImageApi
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.ResponseBody

@Singleton
class RemoteImageDataSource @Inject constructor(
    private val imageApi: ImageApi,
    @ApplicationContext private val context: Context
) {
    suspend fun getRandomImage(): Response<ResponseBody> {
        return imageApi.getRandomImage()
    }
} 