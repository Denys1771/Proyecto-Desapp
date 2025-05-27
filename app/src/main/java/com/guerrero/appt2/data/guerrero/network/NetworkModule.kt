package com.guerrero.appt2.data.guerrero.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val IMGBB_API_KEY = "edcc783e73991b78a37981eab03a3fda" // Reemplaza con tu clave API
    private const val BASE_URL = "https://api.imgbb.com/"

    fun provideImgBBService(): ImgBBService {
        val okHttpClient = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ImgBBService::class.java)
    }

    fun getApiKey(): String = IMGBB_API_KEY
}