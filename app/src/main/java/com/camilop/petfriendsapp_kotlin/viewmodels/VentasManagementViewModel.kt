package com.camilop.petfriendsapp_kotlin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camilop.petfriendsapp_kotlin.models.VentaAdmin
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class VentasManagementViewModel : ViewModel() {

    private val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    private val _sales = MutableLiveData<List<VentaAdmin>>()
    val sales: LiveData<List<VentaAdmin>> get() = _sales

    private val _totalVentasHoy = MutableLiveData<Int>()
    val totalVentasHoy: LiveData<Int> get() = _totalVentasHoy

    private val _ingresosHoy = MutableLiveData<Double>()
    val ingresosHoy: LiveData<Double> get() = _ingresosHoy

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadSales() {
        _loading.value = true
        _error.value = ""

        viewModelScope.launch {
            try {
                println("DEBUG: Cargando ventas...")

                apiService.getAllVentas().enqueue(object : retrofit2.Callback<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse>,
                        response: retrofit2.Response<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse>
                    ) {
                        println("DEBUG: Respuesta ventas - Código: ${response.code()}")

                        if (response.isSuccessful) {
                            val ventas = response.body()?.resultado ?: emptyList()
                            println("DEBUG: Ventas obtenidas: ${ventas.size}")

                            _sales.value = ventas
                            calculateTodayStats(ventas)
                        } else {
                            val errorMsg = "Error al cargar ventas: ${response.code()}"
                            println("DEBUG: $errorMsg")
                            _error.value = errorMsg
                            _sales.value = emptyList()
                        }
                        _loading.value = false
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.VentasAdminResponse>,
                        t: Throwable
                    ) {
                        val errorMsg = "Fallo al cargar ventas: ${t.message}"
                        println("DEBUG: $errorMsg")
                        _error.value = errorMsg
                        _sales.value = emptyList()
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

    private fun calculateTodayStats(ventas: List<VentaAdmin>) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val ventasHoy = ventas.filter { it.fecha == today }
        val ingresosHoy = ventasHoy.sumOf { it.valorPagar }

        _totalVentasHoy.value = ventasHoy.size
        _ingresosHoy.value = ingresosHoy
    }

    fun filterSalesByDate(date: String) {
        val currentSales = _sales.value ?: emptyList()
        if (date.isBlank()) {
            _sales.value = currentSales
        } else {
            val filtered = currentSales.filter { it.fecha == date }
            _sales.value = filtered
        }
    }
}