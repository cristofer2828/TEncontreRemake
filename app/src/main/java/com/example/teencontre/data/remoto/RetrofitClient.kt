package com.example.teencontre.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://tencontre-hkfrcbh9d6fhepdu.canadacentral-01.azurewebsites.net/"

    val instance: AzureApiService by lazy {

        val customGson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.java, JsonDeserializer<Boolean> { json, _, _ ->
                if (json.isJsonPrimitive) {
                    val primitive = json.asJsonPrimitive
                    if (primitive.isBoolean) {
                        return@JsonDeserializer primitive.asBoolean
                    }
                    if (primitive.isNumber) {
                        return@JsonDeserializer primitive.asInt == 1
                    }
                    if (primitive.isString) {
                        val str = primitive.asString
                        return@JsonDeserializer str == "1" || str.lowercase() == "true"
                    }
                }
                false
            })
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .build()

        retrofit.create(AzureApiService::class.java)
    }
}