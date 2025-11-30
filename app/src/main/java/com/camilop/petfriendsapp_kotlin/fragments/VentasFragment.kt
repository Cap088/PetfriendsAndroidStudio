package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.VentasAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentSalesBinding
import com.camilop.petfriendsapp_kotlin.models.SaleHeader
import com.camilop.petfriendsapp_kotlin.models.VentasHeaderResponse
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import com.camilop.petfriendsapp_kotlin.utils.SessionManager
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VentasFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private val salesService = RetrofitClient.apiService
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        loadSalesWithRealClientId()
    }

    private fun loadSalesWithRealClientId() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvSalesList.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                //  Primero obtener el cliente ID real del usuario actual
                val usuario = sessionManager.getUserDetails()?.usuario
                if (usuario.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toasty.error(requireContext(), "Error: Usuario no encontrado", Toasty.LENGTH_LONG).show()
                        showEmptyMessage("Error de sesión")
                    }
                    return@launch
                }

                println(" Obteniendo cliente para usuario: $usuario")

                // Llamar al endpoint que obtiene el cliente por usuario
                val clienteCall = RetrofitClient.apiService.getClientePorUsuario(usuario)
                val clienteResponse = clienteCall.execute()

                if (clienteResponse.isSuccessful && clienteResponse.body() != null) {
                    val clienteData = clienteResponse.body()!!

                    if (clienteData.codigo == "200" && clienteData.resultado != null) {
                        val clientId = if (!clienteData.resultado.esAdmin) {
                            // Si es cliente normal, usar su ID real
                            clienteData.resultado.cliente?.idCliente ?: 0
                        } else {
                            // Si es admin, podemos mostrar todas las ventas o un mensaje
                            // Por ahora usamos 0 para que no muestre ventas de cliente específico
                            0
                        }

                        println(" ClientId obtenido: $clientId")

                        if (clientId == 0) {
                            withContext(Dispatchers.Main) {
                                binding.progressBar.visibility = View.GONE
                                if (clienteData.resultado.esAdmin) {
                                    showEmptyMessage("Los administradores no tienen historial de compras personal")
                                } else {
                                    showEmptyMessage("No se pudo obtener el ID del cliente")
                                }
                            }
                            return@launch
                        }

                        // Ahora cargar las ventas con el clientId real
                        loadSalesList(clientId)

                    } else {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            Toasty.error(requireContext(), "Error: ${clienteData.mensaje}", Toasty.LENGTH_LONG).show()
                            showEmptyMessage("Error al obtener datos del cliente")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toasty.error(requireContext(), "Error al obtener cliente", Toasty.LENGTH_LONG).show()
                        showEmptyMessage("Error de conexión")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toasty.error(requireContext(), "Error: ${e.message}", Toasty.LENGTH_LONG).show()
                    showEmptyMessage("Error de red")
                }
            }
        }
    }

    private fun loadSalesList(clientId: Int) {
        println("Cargando ventas para cliente ID: $clientId")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val call = salesService.getVentasPorCliente(clientId)
                val response = call.execute()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val ventasResponse = response.body() as VentasHeaderResponse

                        println(" Respuesta API: ${ventasResponse.mensaje}")
                        println(" Ventas obtenidas: ${ventasResponse.resultado?.size ?: 0}")

                        if (ventasResponse.codigo == "200" && ventasResponse.resultado != null) {
                            setupRecyclerView(ventasResponse.resultado)
                        } else {
                            showEmptyMessage("No tienes compras registradas")
                        }
                    } else {
                        showEmptyMessage("Error al cargar las ventas")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    showEmptyMessage("Error de conexión")
                }
            }
        }
    }

    private fun setupRecyclerView(sales: List<SaleHeader>) {
        if (sales.isEmpty()) {
            showEmptyMessage("Aún no tienes compras registradas.")
            return
        }

        binding.rvSalesList.visibility = View.VISIBLE
        binding.rvSalesList.layoutManager = LinearLayoutManager(context)

        println("Mostrando ${sales.size} ventas:")
        sales.forEach { venta ->
            println("   - Venta #${venta.idVenta}, Total: ${venta.valorPagar}")
        }

        binding.rvSalesList.adapter = VentasAdapter(sales, null)
    }

    private fun showEmptyMessage(message: String) {
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
        binding.rvSalesList.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}