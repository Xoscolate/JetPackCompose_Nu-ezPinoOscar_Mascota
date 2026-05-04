package com.example.jetpackapploginmvvm.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class MensajeApi(
    val fact: String
)

// 2. La ruta
interface ApiService {
    @GET("fact")
    suspend fun obtenerOraculo(): MensajeApi
}

// 3. El motor de Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://raw.githubusercontent.com/Xoscolate/JetPackCompose_Nu-ezPinoOscar_Mascota/refs/heads/consell/consell.json" // URL base

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}