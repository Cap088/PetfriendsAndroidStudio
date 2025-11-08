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
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VentasFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    // Usamos la instancia 'apiService' del objeto RetrofitClient
    private val salesService = RetrofitClient.apiService

    // usar el id del usuario en sesión real
    private val MOCK_CLIENT_ID = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSalesList(MOCK_CLIENT_ID)
    }

    private fun loadSalesList(userId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvSalesList.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Obtenemos la llamada (Call) de Retrofit
                val call = salesService.getVentasPorCliente(userId)
                val response = call.execute()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {

                        val ventasResponse = response.body() as VentasHeaderResponse

                        if (ventasResponse.codigo == "200" && ventasResponse.resultado != null) {
                            setupRecyclerView(ventasResponse.resultado)
                        } else {
                            // Manejo de error de la API (código != 200)
                            Toasty.error(
                                requireContext(),
                                "Error al cargar compras: ${ventasResponse.mensaje}",
                                Toasty.LENGTH_LONG
                            ).show()
                            showEmptyMessage("No se pudo cargar el historial de compras.")
                        }
                    } else {
                        // Manejo de error HTTP (404, 500, etc.)
                        Toasty.error(
                            requireContext(),
                            "Error HTTP ${response.code()}: ${response.message()}",
                            Toasty.LENGTH_LONG
                        ).show()
                        showEmptyMessage("Error de red. Intenta más tarde.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toasty.error(
                        requireContext(),
                        "Error de conexión: ${e.message}",
                        Toasty.LENGTH_LONG
                    ).show()
                    showEmptyMessage("Error de red. Intenta más tarde.")
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

        // CAMBIO AQUÍ: Sin click listener para facturas
        binding.rvSalesList.adapter = VentasAdapter(sales, null) // ← null para desactivar clics
    }

    private fun showEmptyMessage(message: String) {
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}