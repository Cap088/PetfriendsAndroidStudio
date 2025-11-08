package com.camilop.petfriendsapp_kotlin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AdminDashboardViewModel : ViewModel() {

    private val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    private val _totalUsuarios = MutableLiveData<Int>()
    val totalUsuarios: LiveData<Int> = _totalUsuarios

    private val _totalClientes = MutableLiveData<Int>()
    val totalClientes: LiveData<Int> = _totalClientes

    private val _totalVentas = MutableLiveData<Int>()
    val totalVentas: LiveData<Int> = _totalVentas

    private val _totalIngresos = MutableLiveData<Double>()
    val totalIngresos: LiveData<Double> = _totalIngresos

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadDashboardData() {
        _loading.value = true
        _error.value = ""

        viewModelScope.launch {
            try {
                // Cargar datos secuencialmente para evitar problemas
                loadUsuarios()
                loadClientes()
                loadVentas()

            } catch (e: Exception) {
                _error.value = "Error general: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadUsuarios() {
        apiService.getUsuarios().enqueue(object : retrofit2.Callback<com.camilop.petfriendsapp_kotlin.models.UsuarioListResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.UsuarioListResponse>,
                response: retrofit2.Response<com.camilop.petfriendsapp_kotlin.models.UsuarioListResponse>
            ) {
                if (response.isSuccessful) {
                    val usuarios = response.body()?.resultado?.size ?: 0
                    _totalUsuarios.value = usuarios
                } else {
                    _totalUsuarios.value = 0
                    _error.value = "Error usuarios: ${response.code()}"
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.UsuarioListResponse>,
                t: Throwable
            ) {
                _totalUsuarios.value = 0
                _error.value = "Fallo usuarios: ${t.message}"
            }
        })
    }

    private fun loadClientes() {
        apiService.getClientes().enqueue(object : retrofit2.Callback<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>,
                response: retrofit2.Response<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()?.resultado?.size ?: 0
                    _totalClientes.value = clientes
                } else {
                    _totalClientes.value = 0
                    _error.value = "Error clientes: ${response.code()}"
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.ClienteListResponse>,
                t: Throwable
            ) {
                _totalClientes.value = 0
                _error.value = "Fallo clientes: ${t.message}"
            }
        })
    }

    private fun loadVentas() {
        apiService.getAllVentas().enqueue(object : retrofit2.Callback<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse>,
                response: retrofit2.Response<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse>
            ) {
                if (response.isSuccessful) {
                    val ventas = response.body()?.resultado?.size ?: 0
                    val ingresos = response.body()?.resultado?.sumOf { it.valorPagar } ?: 0.0

                    _totalVentas.value = ventas
                    _totalIngresos.value = ingresos
                } else {
                    _totalVentas.value = 0
                    _totalIngresos.value = 0.0
                    _error.value = "Error ventas: ${response.code()}"
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse>,
                t: Throwable
            ) {
                _totalVentas.value = 0
                _totalIngresos.value = 0.0
                _error.value = "Fallo ventas: ${t.message}"
            }
        })
    }
}