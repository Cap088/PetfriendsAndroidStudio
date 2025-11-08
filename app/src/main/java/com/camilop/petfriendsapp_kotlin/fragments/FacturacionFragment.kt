package com.camilop.petfriendsapp_kotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.Facturas.FacturaActivity
import com.camilop.petfriendsapp_kotlin.VentasAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentFacturacionBinding
import com.camilop.petfriendsapp_kotlin.models.ClienteResponse
import com.camilop.petfriendsapp_kotlin.models.VentasHeaderResponse
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import com.camilop.petfriendsapp_kotlin.utils.SessionManager
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FacturacionFragment : Fragment() {

    private lateinit var binding: FragmentFacturacionBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private lateinit var ventasAdapter: VentasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacturacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        initRetrofit()
        setupRecyclerView()
        loadClienteYVentas()
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun setupRecyclerView() {
        ventasAdapter = VentasAdapter(emptyList()) { venta ->
            generarFacturaPDF(venta.idVenta)
        }

        binding.rvVentas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ventasAdapter
        }
    }

    private fun generarFacturaPDF(idVenta: Int) {
        Toasty.info(requireContext(), "Generando factura PDF para venta #$idVenta", Toasty.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), FacturaActivity::class.java).apply {
            putExtra("ID_VENTA", idVenta)
        }
        startActivity(intent)
    }

    private fun loadClienteYVentas() {
        val user = sessionManager.getUserDetails()
        if (user == null) {
            showError("No se pudo obtener información del usuario")
            return
        }

        showLoading()

        apiService.getClientePorUsuario(user.usuario).enqueue(object : Callback<ClienteResponse> {
            override fun onResponse(call: Call<ClienteResponse>, response: Response<ClienteResponse>) {
                if (response.isSuccessful) {
                    val clienteResponse = response.body()
                    if (clienteResponse?.codigo == "200") {
                        val resultado = clienteResponse.resultado

                        if (resultado?.esAdmin == true) {
                            // Usuario es administrador
                            Toasty.success(requireContext(), "Modo Administrador", Toasty.LENGTH_SHORT).show()
                            loadAllVentas()
                        } else {
                            // Usuario normal
                            val cliente = resultado?.cliente
                            if (cliente != null) {
                                Toasty.success(requireContext(), "Cliente: ${cliente.nombre}", Toasty.LENGTH_SHORT).show()
                                loadVentas(cliente.idCliente)
                            } else {
                                hideLoading()
                                showError("Cliente no encontrado")
                            }
                        }
                    } else {
                        hideLoading()
                        showError(clienteResponse?.mensaje ?: "Error al obtener información")
                    }
                } else {
                    hideLoading()
                    showError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ClienteResponse>, t: Throwable) {
                hideLoading()
                showError("Error de conexión: ${t.message}")
            }
        })
    }

    private fun loadAllVentas() {
        // Por ahora, si no tienes el endpoint de todas las ventas, mostramos mensaje
        hideLoading()
        showError("Funcionalidad de administrador en desarrollo")
        // apiService.getAllVentas().enqueue(...) // Comentado hasta que implementes el endpoint
    }

    private fun loadVentas(clienteId: Int) {
        apiService.getVentasPorCliente(clienteId).enqueue(object : Callback<VentasHeaderResponse> {
            override fun onResponse(call: Call<VentasHeaderResponse>, response: Response<VentasHeaderResponse>) {
                hideLoading()

                if (response.isSuccessful) {
                    val ventasResponse = response.body()
                    if (ventasResponse?.codigo == "200") {
                        val ventas = ventasResponse.resultado ?: emptyList()
                        if (ventas.isEmpty()) {
                            showEmptyState()
                        } else {
                            hideEmptyState()
                            ventasAdapter.updateSales(ventas)
                        }
                    } else {
                        showError(ventasResponse?.mensaje ?: "Error al cargar ventas")
                    }
                } else {
                    showError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<VentasHeaderResponse>, t: Throwable) {
                hideLoading()
                showError("Error de conexión: ${t.message}")
            }
        })
    }

    // Funciones auxiliares para manejar estados de la UI
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvVentas.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvVentas.visibility = View.VISIBLE
    }

    private fun showError(mensaje: String) {
        Toasty.error(requireContext(), mensaje, Toasty.LENGTH_SHORT).show()
    }
}