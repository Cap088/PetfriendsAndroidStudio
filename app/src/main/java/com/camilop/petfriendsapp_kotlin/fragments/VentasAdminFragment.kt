package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.adapters.VentasAdminAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentVentasManagementBinding
import com.camilop.petfriendsapp_kotlin.models.VentaAdmin
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class VentasAdminFragment : Fragment() {

    private var _binding: FragmentVentasManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var ventasAdapter: VentasAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVentasManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadAllVentas()
    }

    private fun setupRecyclerView() {
        ventasAdapter = VentasAdminAdapter(
            onViewDetails = { venta ->
                verDetallesVenta(venta)
            },
            onPrintInvoice = { venta ->
                imprimirFactura(venta)
            }
        )

        binding.rvSales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSales.adapter = ventasAdapter
    }

    private fun setupClickListeners() {
        binding.btnFilter.setOnClickListener {
            val fecha = binding.etFilterDate.text.toString().trim()
            filtrarVentasPorFecha(fecha)
        }

        // Mostrar fechas disponibles como hint
        binding.etFilterDate.hint = "YYYY-MM-DD (ej: 2024-01-15)"

        // Agregar botón para ver todas las ventas
        val btnVerTodas = android.widget.Button(requireContext()).apply {
            text = "Ver Todas"
            setOnClickListener {
                binding.etFilterDate.text?.clear()
                loadAllVentas()
            }
        }

        binding.etFilterDate.setOnClickListener {
            // Mostrar fechas disponibles cuando se hace clic
            val ventasActuales = ventasAdapter.currentList
            if (ventasActuales.isNotEmpty()) {
                val fechas = ventasActuales.map { it.fecha }.distinct().sorted().take(3)
                if (fechas.isNotEmpty()) {
                    Toasty.info(
                        requireContext(),
                        "Fechas disponibles: ${fechas.joinToString(", ")}",
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadAllVentas() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.rvSales.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {

                val call = RetrofitClient.apiService.getAllVentas()
                val response = call.execute()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val ventasResponse = response.body()!!

                        if (ventasResponse.codigo == "200" && ventasResponse.resultado != null) {
                            setupVentasList(ventasResponse.resultado)
                            calcularEstadisticasHoy(ventasResponse.resultado)
                        } else {
                            Toasty.error(
                                requireContext(),
                                "Error: ${ventasResponse.mensaje}",
                                Toasty.LENGTH_LONG
                            ).show()
                            mostrarMensajeVacio("No se pudieron cargar las ventas")
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

    private fun setupVentasList(ventas: List<VentaAdmin>) {
        println("🔍 DEBUG: === FECHAS RECIBIDAS ===")
        ventas.forEach { venta ->
            println("   📅 Venta #${venta.idVenta}: '${venta.fecha}'")
        }
        println("=================================")

        if (ventas.isEmpty()) {
            mostrarMensajeVacio("No hay ventas registradas")
            return
        }

        binding.rvSales.visibility = View.VISIBLE
        ventasAdapter.submitList(ventas)
    }

    private fun filtrarVentasPorFecha(fecha: String) {
        println("🔄 DEBUG: Filtrando por fecha: '$fecha'")

        val ventasActuales = ventasAdapter.currentList
        println("📊 DEBUG: Ventas actuales: ${ventasActuales.size}")

        // MOSTRAR FECHAS DISPONIBLES
        println("📅 DEBUG: === FECHAS DISPONIBLES ===")
        val fechasUnicas = ventasActuales.map { it.fecha }.distinct().sorted()
        fechasUnicas.forEach { fechaDisponible ->
            println("   📆 $fechaDisponible")
        }
        println("=================================")

        if (fecha.isBlank()) {
            // Mostrar todas las ventas
            ventasAdapter.submitList(ventasActuales)
            binding.tvEmptyState.visibility = View.GONE
            binding.rvSales.visibility = View.VISIBLE
            Toasty.info(requireContext(), "Mostrando todas las ventas", Toasty.LENGTH_SHORT).show()
            return
        }

        // Validar formato de fecha
        val fechaRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
        if (!fecha.matches(fechaRegex)) {
            Toasty.error(
                requireContext(),
                "Formato inválido. Use: YYYY-MM-DD",
                Toasty.LENGTH_LONG
            ).show()
            return
        }

        // Filtrar
        val ventasFiltradas = ventasActuales.filter { it.fecha == fecha }

        if (ventasFiltradas.isEmpty()) {
            // Mostrar sugerencias de fechas cercanas
            val fechasSugeridas = fechasUnicas.take(3)
            val mensajeSugerencias = if (fechasSugeridas.isNotEmpty()) {
                "\nFechas disponibles: ${fechasSugeridas.joinToString(", ")}"
            } else {
                ""
            }

            mostrarMensajeVacio("No hay ventas para: $fecha$mensajeSugerencias")
            binding.rvSales.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvSales.visibility = View.VISIBLE
            ventasAdapter.submitList(ventasFiltradas)

            Toasty.success(
                requireContext(),
                "${ventasFiltradas.size} ventas para $fecha",
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun calcularEstadisticasHoy(ventas: List<VentaAdmin>) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val ventasHoy = ventas.filter { it.fecha == hoy }
        val ingresosHoy = ventasHoy.sumOf { it.valorPagar }

        binding.tvTotalVentasHoy.text = ventasHoy.size.toString()
        binding.tvIngresosHoy.text = "$${String.format("%.2f", ingresosHoy)}"
    }

    private fun verDetallesVenta(venta: VentaAdmin) {
        // Navegar a detalles de venta
        Toasty.info(requireContext(), "Ver detalles venta #${venta.idVenta}", Toasty.LENGTH_SHORT).show()
        // findNavController().navigate(R.id.action_ventasAdmin_to_ventaDetailFragment,
        //     bundleOf("ventaId" to venta.idVenta))
    }

    private fun imprimirFactura(venta: VentaAdmin) {
        // Generar factura
        Toasty.success(requireContext(), "Generando factura #${venta.idVenta}", Toasty.LENGTH_SHORT).show()
    }

    private fun mostrarMensajeVacio(mensaje: String) {
        binding.tvEmptyState.text = mensaje
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvSales.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}