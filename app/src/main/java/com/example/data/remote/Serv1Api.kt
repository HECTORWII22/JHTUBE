package com.example.data.remote

import com.example.data.model.Serv1Item
import com.example.data.model.VideoStreamResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface Serv1Api {
    @GET("trending")
    suspend fun getTrending(): List<Serv1Item>

    @GET("search")
    suspend fun search(@Query("q") query: String): List<Serv1Item>

    @GET("video")
    suspend fun getVideoDetails(
        @Query("id") id: String,
        @Query("audio") audio: Int = 0
    ): Response<VideoStreamResponse>

    @Streaming
    @GET("dl")
    suspend fun downloadMedia(
        @Query("id") id: String,
        @Query("audio") audio: Int = 0
    ): Response<ResponseBody>
}
