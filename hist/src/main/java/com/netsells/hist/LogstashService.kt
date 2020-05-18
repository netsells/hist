package com.netsells.hist

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface LogstashService {

    @POST("/")
    suspend fun sendLog(@Body log: JsonObject)

    companion object {
        fun create(baseUrl: String): LogstashService {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(
                            HttpLoggingInterceptor()
                                .setLevel(HttpLoggingInterceptor.Level.BASIC)
                        )
                        .callTimeout(10, TimeUnit.SECONDS)
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create()
        }
    }
}