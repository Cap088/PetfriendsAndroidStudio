package com.camilop.petfriendsapp_kotlin.Facturas

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camilop.petfriendsapp_kotlin.models.Cliente
import com.camilop.petfriendsapp_kotlin.models.VentaCompleta
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import kotlinx.coroutines.launch
import com.camilop.petfriendsapp_kotlin.models.SaleDetailResponse
import com.camilop.petfriendsapp_kotlin.utils.SessionManager
import retrofit2.Callback
import retrofit2.Response


class FacturaViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService

    private val sessionManager = SessionManager(application)

    private val _facturaData = MutableLiveData<VentaCompleta?>()
    val facturaData: LiveData<VentaCompleta?> = _facturaData

    private val _clienteData = MutableLiveData<Cliente?>()
    val clienteData: LiveData<Cliente?> = _clienteData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFacturaData(idVenta: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                apiService.getVentaDetail(idVenta).enqueue(object : Callback<SaleDetailResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<SaleDetailResponse>,
                        response: Response<SaleDetailResponse>
                    ) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.codigo == "200" && apiResponse.resultado != null) {

                                val ventaCompleta = apiResponse.resultado
                                _facturaData.postValue(ventaCompleta)

                                val userSession = sessionManager.getUserDetails()
                                val cliente = Cliente(
                                    idCliente = ventaCompleta.cabecera.idCliente,
                                    nombre = ventaCompleta.cabecera.cliente_nombre ?: "",
                                    apellido = ventaCompleta.cabecera.cliente_apellido ?: "",
                                    cedula = ventaCompleta.cabecera.cliente_cedula ?: "",
                                    telefono = userSession?.telefono ?: "",
                                    direccion = ventaCompleta.cabecera.cliente_direccion ?: ""
                                )
                                _clienteData.postValue(cliente)

                            } else {
                                _error.postValue("API: ${apiResponse?.mensaje ?: "Respuesta inválida"}")
                            }
                        } else {
                            _error.postValue("HTTP ${response.code()}: ${response.message()}")
                        }
                        _loading.postValue(false)
                    }

                    override fun onFailure(call: retrofit2.Call<SaleDetailResponse>, t: Throwable) {
                        _error.postValue("Error de conexión: ${t.message}")
                        println("EXCEPCIÓN: ${t.message}")
                        _loading.postValue(false)
                    }
                })

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                println("EXCEPCIÓN: ${e.message}")
                _loading.value = false
            }
        }
    }
}