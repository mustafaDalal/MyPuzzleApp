package com.md.mypuzzleapp.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path

interface ImageApi {
    @GET("{size}")
    suspend fun getRandomImage(@Path("size") size: Int = 1200): retrofit2.Response<okhttp3.ResponseBody>
    
    companion object {
        const val BASE_URL = "https://picsum.photos/"
    }
}