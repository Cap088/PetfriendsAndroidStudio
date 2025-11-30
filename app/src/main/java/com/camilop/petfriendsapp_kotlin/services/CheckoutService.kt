package com.camilop.petfriendsapp_kotlin.services

import com.camilop.petfriendsapp_kotlin.models.VentaRequest
import com.camilop.petfriendsapp_kotlin.models.VentaResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

//Clase para manejar las llamadas a la API de ventas usando OkHttp.
// NOTA: Requiere las dependencias OkHttp y Gson en build.gradle
class CheckoutService {

    // URL base para el emulador de Android.
    private val BASE_URL = "http://10.0.2.2:3000"
    private val client = OkHttpClient()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    //Envía la solicitud de registro de venta al backend.
    fun registrarVenta(request: VentaRequest): VentaResponse? {
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody(JSON)

        val url = "$BASE_URL/ventas/add"

        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            val response: Response = client.newCall(httpRequest).execute()

            if (!response.isSuccessful) throw IOException("Código de respuesta inesperado: ${response.code}")

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) throw IOException("Respuesta vacía del servidor.")

            // Convertir la respuesta JSON a VentaResponse
            gson.fromJson(responseBody, VentaResponse::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
            // Retorna un objeto de error para que el Fragment lo maneje
            VentaResponse(
                codigo = "500",
                mensaje = "Error de red/servidor: ${e.message}",
                error = e.message,
                resultado = null  // ✅ CORREGIDO: Cambiar TODO() por null
            )
        }
    }
}