package com.md.mypuzzleapp.data.remote.api

import retrofit2.http.GET

interface ImageApi {
    @GET("1080/720")
    suspend fun getRandomImage(): retrofit2.Response<okhttp3.ResponseBody>
    
    companion object {
        const val BASE_URL = "https://picsum.photos/"
    }
} 