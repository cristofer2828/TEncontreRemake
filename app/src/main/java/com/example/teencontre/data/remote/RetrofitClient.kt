package com.example.teencontre.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "https://tencontre-hkfrcbh9d6fhepdu.canadacentral-01.azurewebsites.net"

    val instance: AzureApiService by lazy {

        val logging = HttpLoggingInterceptor {
            Log.d("RETROFIT", it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Lógica de conversión reutilizable para primitivos y objetos mutables
        val booleanDeserializer = JsonDeserializer<Boolean?> { json, _, _ ->
            if (json == null || json.isJsonNull) {
                return@JsonDeserializer null
            }

            if (json.isJsonPrimitive) {
                val primitive = json.asJsonPrimitive

                if (primitive.isBoolean)
                    return@JsonDeserializer primitive.asBoolean

                if (primitive.isNumber)
                    return@JsonDeserializer (primitive.asInt == 1)

                if (primitive.isString) {
                    val str = primitive.asString.lowercase()
                    if (str == "1" || str == "true") return@JsonDeserializer true
                    if (str == "0" || str == "false") return@JsonDeserializer false
                }
            }
            null // 🌟 Cambiar 'false' por 'null' para respetar los campos vacíos de tu BD
        }

        val customGson = GsonBuilder()
            // 1. Registra para tipos primitivos de Kotlin/Java (boolean)
            .registerTypeAdapter(Boolean::class.java, booleanDeserializer)
            // 2. Registra para tipos Objetos/Nullables en Kotlin (Boolean?) 🌟¡CLAVE!🌟
            .registerTypeAdapter(Boolean::class.javaObjectType, booleanDeserializer)
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(customGson)
            )
            .build()
            .create(AzureApiService::class.java)
    }
}