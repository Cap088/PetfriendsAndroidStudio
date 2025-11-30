package com.camilop.petfriendsapp_kotlin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camilop.petfriendsapp_kotlin.models.Product
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.gson.Gson
import java.io.OutputStreamWriter
import java.net.HttpURLConnection

class GestionProductosViewModel : ViewModel() {

    private val _productos = MutableLiveData<List<Product>>()
    val productos: LiveData<List<Product>> = _productos

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val baseUrl = APIConfig.BASE_URL

    fun cargarProductos() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Ejecutar en el dispatcher de IO para operaciones de red
                val productosList = withContext(Dispatchers.IO) {
                    realizarPeticionProductos()
                }
                _productos.value = productosList
                println("DEBUG: ${productosList.size} productos cargados")
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message ?: "Desconocido"}"
                println("DEBUG: Excepción: ${e.message}")
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun realizarPeticionProductos(): List<Product> {
        return withContext(Dispatchers.IO) {
            val urlString = "${baseUrl.removeSuffix("/")}/productos"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")

            println("DEBUG: Intentando conectar a: $url")

            val responseCode = connection.responseCode
            println("DEBUG: Código de respuesta: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }

                println("DEBUG: Respuesta recibida: $response")

                val gson = Gson()
                val apiResponse = gson.fromJson(response, ApiResponse::class.java)

                if (apiResponse.codigo == "200") {
                    apiResponse.resultado
                } else {
                    throw Exception("Error API: ${apiResponse.mensaje}")
                }
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalles"
                throw Exception("Error HTTP $responseCode: $errorResponse")
            }
        }
    }

    fun agregarProducto(producto: Product) {
        viewModelScope.launch {
            try {
                _loading.value = true
                withContext(Dispatchers.IO) {
                    realizarAgregarProducto(producto)
                }
                _mensaje.value = "Producto agregado exitosamente"
                cargarProductos() // Recargar la lista
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun realizarAgregarProducto(producto: Product) {
        withContext(Dispatchers.IO) {
            val urlString = "${baseUrl.removeSuffix("/")}/productos/add"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val productData = HashMap<String, Any>().apply {
                put("nombre", producto.nombre)
                put("cantidad", producto.cantidad)
                put("precio", producto.precio)
                put("descripcion", producto.descripcion)
                put("porcentajeIva", producto.porcentajeIva.toInt())
                put("idCategoria", 1)
                put("imagenes", producto.imagenes ?: "")
            }

            val gson = Gson()
            val jsonInputString = gson.toJson(productData)

            println("DEBUG: Enviando datos: $jsonInputString")

            val outputStream = connection.outputStream
            OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                writer.write(jsonInputString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            println("DEBUG: Respuesta agregar: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val apiResponse = gson.fromJson(response, ApiResponse::class.java)

                if (apiResponse.codigo != "200") {
                    throw Exception(apiResponse.mensaje)
                }
            } else {
                throw Exception("Error HTTP: $responseCode")
            }
        }
    }

    fun actualizarProducto(producto: Product) {
        viewModelScope.launch {
            try {
                _loading.value = true
                withContext(Dispatchers.IO) {
                    realizarActualizarProducto(producto)
                }
                _mensaje.value = "Producto actualizado exitosamente"
                cargarProductos()
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun realizarActualizarProducto(producto: Product) {
        withContext(Dispatchers.IO) {
            val urlString = "${baseUrl.removeSuffix("/")}/productos/update/${producto.idProducto}"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val productData = HashMap<String, Any>().apply {
                put("nombre", producto.nombre)
                put("descripcion", producto.descripcion)
                put("cantidad", producto.cantidad)
                put("precio", producto.precio)
                put("porcentajeIva", producto.porcentajeIva.toInt())
                put("idCategoria", 1)
                put("imagenes", producto.imagenes ?: "")
            }

            val gson = Gson()
            val jsonInputString = gson.toJson(productData)

            val outputStream = connection.outputStream
            OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                writer.write(jsonInputString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val apiResponse = gson.fromJson(response, ApiResponse::class.java)

                if (apiResponse.codigo != "200") {
                    throw Exception(apiResponse.mensaje)
                }
            } else {
                throw Exception("Error HTTP: $responseCode")
            }
        }
    }

    fun eliminarProducto(productoId: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                withContext(Dispatchers.IO) {
                    realizarEliminarProducto(productoId)
                }
                _mensaje.value = "Producto eliminado exitosamente"
                cargarProductos()
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun realizarEliminarProducto(productoId: Int) {
        withContext(Dispatchers.IO) {
            val urlString = "${baseUrl.removeSuffix("/")}/productos/delete/$productoId"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val apiResponse = gson.fromJson(response, ApiResponse::class.java)

                if (apiResponse.codigo != "200") {
                    throw Exception(apiResponse.mensaje)
                }
            } else {
                throw Exception("Error HTTP: $responseCode")
            }
        }
    }

    // Clase para parsear la respuesta de la API
    data class ApiResponse(
        val codigo: String,
        val mensaje: String,
        val resultado: List<Product>
    )
}