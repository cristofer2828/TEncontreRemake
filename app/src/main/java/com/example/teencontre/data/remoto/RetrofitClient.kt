package com.example.teencontre.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Aquí pondrás la URL del App Service de Azure cuando lo tengas publicado
    private const val BASE_URL =    "https://tencontre-hkfrcbh9d6fhepdu.canadacentral-01.azurewebsites.net/"

    val instance: AzureApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(AzureApiService::class.java)
    }
}