package com.example.data.remote

import com.example.data.model.AddHistoryRequest
import com.example.data.model.HistoryItemRemote
import com.example.data.model.LoginRequest
import com.example.data.model.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Serv0Api {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("add")
    suspend fun addHistory(@Body request: AddHistoryRequest): Response<ResponseBody>

    @GET("list")
    suspend fun listHistory(@Query("token") token: String? = null): Response<List<HistoryItemRemote>>
}
