package com.camilop.petfriendsapp_kotlin.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // URL base del servidor (ajustar si es necesario)
    private const val BASE_URL = "http://10.0.2.2:3000"

    // Instancia de Retrofit, configurada para usar Gson para la conversión de JSON
    val api: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Agrega el convertidor de Gson para manejar los Data Classes
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
