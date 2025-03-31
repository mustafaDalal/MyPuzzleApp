package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.data.remote.datasource.RemoteImageDataSource
import retrofit2.Response
import okhttp3.ResponseBody
import javax.inject.Inject

class GetRandomImageUseCase @Inject constructor(
    private val remoteImageDataSource: RemoteImageDataSource
) {
    suspend operator fun invoke(): Response<ResponseBody> {
        return remoteImageDataSource.getRandomImage()
    }
} 