package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.adapters.VentaDetalleAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentVentaDetalleBinding
import com.camilop.petfriendsapp_kotlin.models.VentaCompleta
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class VentaDetalleFragment : Fragment() {

    private var _binding: FragmentVentaDetalleBinding? = null
    private val binding get() = _binding!!
    private lateinit var detalleAdapter: VentaDetalleAdapter
    private val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    private var ventaId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVentaDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ID de la venta de los argumentos
        ventaId = arguments?.getInt("ventaId", 0) ?: 0

        if (ventaId == 0) {
            Toasty.error(requireContext(), "Error: ID de venta no válido", Toasty.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        loadVentaDetalle()
    }

    private fun setupRecyclerView() {
        detalleAdapter = VentaDetalleAdapter()
        binding.rvProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductos.adapter = detalleAdapter
    }

    private fun setupClickListeners() {
        binding.btnVolver.setOnClickListener {
            // Usar popBackStack para regresar al fragment anterior
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadVentaDetalle() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val call = RetrofitClient.apiService.getVentaDetail(ventaId)
                val response = call.execute()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val ventaResponse = response.body()!!

                        if (ventaResponse.codigo == "200" && ventaResponse.resultado != null) {
                            mostrarDetallesVenta(ventaResponse.resultado)
                        } else {
                            Toasty.error(
                                requireContext(),
                                "Error: ${ventaResponse.mensaje}",
                                Toasty.LENGTH_LONG
                            ).show()
                            mostrarMensajeVacio("No se pudieron cargar los detalles")
                        }
                    } else {
                        Toasty.error(
                            requireContext(),
                            "Error HTTP ${response.code()}",
                            Toasty.LENGTH_LONG
                        ).show()
                        mostrarMensajeVacio("Error de conexión")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toasty.error(
                        requireContext(),
                        "Error: ${e.message}",
                        Toasty.LENGTH_LONG
                    ).show()
                    mostrarMensajeVacio("Error de red")
                }
            }
        }
    }

    private fun mostrarDetallesVenta(ventaCompleta: VentaCompleta) {
        val cabecera = ventaCompleta.cabecera
        val detalle = ventaCompleta.detalle

        // Mostrar información de la cabecera
        binding.tvVentaId.text = "Venta #${cabecera.idCabeceraVenta}"
        binding.tvFecha.text = "Fecha: ${cabecera.fechaVenta}"
        binding.tvCliente.text = "Cliente: ${cabecera.cliente_nombre ?: "N/A"} ${cabecera.cliente_apellido ?: ""}"
        binding.tvTotal.text = "Total: ${format.format(cabecera.valorPagar)}"

        // Mostrar metodo de pago
        val metodoPago = if (cabecera.tarjeta != null) {
            "${cabecera.tarjeta} •••• ${cabecera.numeroTarjeta?.takeLast(4) ?: "XXXX"}"
        } else {
            "Efectivo"
        }
        binding.tvMetodoPago.text = "Método de pago: $metodoPago"

        // Mostrar productos
        if (detalle.isNotEmpty()) {
            detalleAdapter.submitList(detalle)
            binding.rvProductos.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        } else {
            mostrarMensajeVacio("No hay productos en esta venta")
        }
    }

    private fun mostrarMensajeVacio(mensaje: String) {
        binding.tvEmptyState.text = mensaje
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvProductos.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}