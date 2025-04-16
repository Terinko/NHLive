package com.example.nhlive.API

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Create an OkHttpClient with logging for debugging API calls
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Create a Gson instance that's more lenient with parsing errors
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Main API for game schedules
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api-web.nhle.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Stats API with different base URL
    val statsRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.nhle.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val statsApiService: ApiService = statsRetrofit.create(ApiService::class.java)
}