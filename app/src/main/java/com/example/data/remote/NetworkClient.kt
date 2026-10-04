package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val SERV1_BASE_URL = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/"
    private const val SERV0_BASE_URL = "https://serv0-history.rejh.workers.dev/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val serv1Api: Serv1Api by lazy {
        Retrofit.Builder()
            .baseUrl(SERV1_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(Serv1Api::class.java)
    }

    val serv0Api: Serv0Api by lazy {
        Retrofit.Builder()
            .baseUrl(SERV0_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(Serv0Api::class.java)
    }

    fun getDlUrl(id: String): String {
        return "${SERV1_BASE_URL}dl?id=$id"
    }
}
