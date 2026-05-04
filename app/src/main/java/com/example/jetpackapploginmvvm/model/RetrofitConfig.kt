package com.example.jetpackapploginmvvm.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class MensajeApi(val fact: String)

interface ApiService {
    @GET("Xoscolate/JetPackCompose_Nu-ezPinoOscar_Mascota/refs/heads/consell/consell.json")
    suspend fun obtenerOraculo(): MensajeApi
}

object RetrofitClient {
    private const val BASE_URL = "https://raw.githubusercontent.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}