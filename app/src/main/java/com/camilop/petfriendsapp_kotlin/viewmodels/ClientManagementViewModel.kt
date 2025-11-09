package com.camilop.petfriendsapp_kotlin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camilop.petfriendsapp_kotlin.models.Cliente
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClientManagementViewModel : ViewModel() {

    private val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    private val _clients = MutableLiveData<List<Cliente>>()
    val clients: LiveData<List<Cliente>> get() = _clients

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Función para cargar clientes
    fun loadClients() {
        _loading.value = true
        _error.value = ""

        viewModelScope.launch {
            try {
                println("DEBUG: Cargando clientes...")

                apiService.getClientes().enqueue(object :
                    Callback<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse> {
                    override fun onResponse(
                        call: Call<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>,
                        response: Response<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>
                    ) {
                        println("DEBUG: Respuesta clientes - Código: ${response.code()}")

                        if (response.isSuccessful) {
                            val clientes = response.body()?.resultado ?: emptyList()
                            println("DEBUG: Clientes obtenidos: ${clientes.size}")

                            _clients.value = clientes
                        } else {
                            val errorMsg = "Error al cargar clientes: ${response.code()}"
                            println("DEBUG: $errorMsg")
                            _error.value = errorMsg
                            _clients.value = emptyList()
                        }
                        _loading.value = false
                    }

                    override fun onFailure(
                        call: Call<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>,
                        t: Throwable
                    ) {
                        val errorMsg = "Fallo al cargar clientes: ${t.message}"
                        println("DEBUG: $errorMsg")
                        _error.value = errorMsg
                        _clients.value = emptyList()
                        _loading.value = false
                    }
                })
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                println("DEBUG: $errorMsg")
                _error.value = errorMsg
                _loading.value = false
            }
        }
    }

    // Función para buscar clientes
    fun searchClients(query: String) {
        _loading.value = true
        _error.value = ""

        viewModelScope.launch {
            try {
                println("DEBUG: Buscando clientes con query: '$query'")

                if (query.isBlank()) {
                    // Si la búsqueda está vacía, cargar todos los clientes
                    loadClients()
                    return@launch
                }

                apiService.searchClientes(query).enqueue(object :
                    Callback<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse> {
                    override fun onResponse(
                        call: Call<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>,
                        response: Response<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>
                    ) {
                        println("DEBUG: Respuesta de búsqueda clientes - Código: ${response.code()}")

                        if (response.isSuccessful) {
                            val clientes = response.body()?.resultado ?: emptyList()
                            println("DEBUG: Clientes encontrados en búsqueda: ${clientes.size}")

                            _clients.value = clientes
                        } else {
                            val errorMsg = "Error en búsqueda de clientes: ${response.code()}"
                            println("DEBUG: $errorMsg")
                            _error.value = errorMsg
                        }
                        _loading.value = false
                    }

                    override fun onFailure(
                        call: Call<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>,
                        t: Throwable
                    ) {
                        val errorMsg = "Fallo en búsqueda de clientes: ${t.message}"
                        println("DEBUG: $errorMsg")
                        _error.value = errorMsg
                        _loading.value = false
                    }
                })
            } catch (e: Exception) {
                val errorMsg = "Error en búsqueda de clientes: ${e.message}"
                println("DEBUG: $errorMsg")
                _error.value = errorMsg
                _loading.value = false
            }
        }
    }
}