package com.camilop.petfriendsapp_kotlin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.camilop.petfriendsapp_kotlin.databinding.FragmentPaymentBinding
import com.camilop.petfriendsapp_kotlin.models.DetalleVentaRequest
import com.camilop.petfriendsapp_kotlin.models.VentaResponse
import com.camilop.petfriendsapp_kotlin.models.VentaRequest
import com.camilop.petfriendsapp_kotlin.services.CheckoutService
import com.camilop.petfriendsapp_kotlin.utils.CartManager
import com.camilop.petfriendsapp_kotlin.utils.SessionManager
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class PagosFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    // Argumentos recibidos del CartFragment
    private var totalAmount: Double = 0.0

    // Tasa del 16%
    private val IVA_RATE = 0.16

    // SessionManager para obtener el ID real del cliente
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val ARG_TOTAL_AMOUNT = "total_amount"

        fun newInstance(totalAmount: Double): PagosFragment {
            val fragment = PagosFragment()
            val args = Bundle().apply {
                putDouble(ARG_TOTAL_AMOUNT, totalAmount)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            totalAmount = it.getDouble(ARG_TOTAL_AMOUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        binding.tvPaymentTotalAmount.text = format.format(totalAmount)

        binding.btnConfirmPayment.setOnClickListener {
            validateAndProcessPayment()
        }
    }

    // LÓGICA DE VALIDACIÓN Y PAGO

    private fun validateAndProcessPayment() {
        // Verificar que el usuario esté logueado
        if (!sessionManager.isLoggedIn()) {
            Toasty.error(
                requireContext(),
                "Debe iniciar sesión para realizar una compra",
                Toasty.LENGTH_LONG
            ).show()
            return
        }

        // Obtener el ID real del cliente
        val clienteId = getCurrentClientId()

        if (clienteId == -1) {
            Toasty.error(
                requireContext(),
                "No se pudo obtener la información del cliente. Por favor, inicie sesión nuevamente.",
                Toasty.LENGTH_LONG
            ).show()
            return
        }

        println("✅ Procesando pago para cliente ID: $clienteId")

        // Validación simple de campos
        val cardNumber = binding.etCardNumber.text.toString()
        val cardHolderName = binding.etCardHolderName.text.toString()
        val expiryDate = binding.etExpiryDate.text.toString()
        val cvv = binding.etCvv.text.toString()

        if (cardNumber.length != 16 || cardHolderName.isEmpty() || expiryDate.isEmpty() || cvv.length != 3) {
            Toasty.error(
                requireContext(),
                "Por favor, complete todos los campos de la tarjeta correctamente.",
                Toasty.LENGTH_SHORT
            ).show()
            return
        }

        // Desactivar UI y mostrar progreso
        setLoading(true)

        // Llamamos a la lógica de pago con el ID real del cliente
        processPayment(cardNumber, clienteId)
    }

    private fun processPayment(cardNumber: String, clienteId: Int) {
        // Reconstruir los detalles de la venta desde el CartManager
        val cartEntries = CartManager.getCartItems()
        val detallesVenta = cartEntries.map { (product, quantity) ->
            val subtotal = product.precio * quantity
            val descuento = 0.0
            val iva = subtotal * IVA_RATE
            val totalPagar = subtotal - descuento + iva

            DetalleVentaRequest(
                idProducto = product.idProducto,
                cantidad = quantity,
                precioUnitario = product.precio,
                subtotal = subtotal,
                descuento = descuento,
                iva = iva,
                totalPagar = totalPagar
            )
        }

        // Crear la solicitud VentaRequest con el ID real del cliente
        val ventaRequest = VentaRequest(
            idCliente = clienteId, // ✅ Usar el ID real del cliente
            valorPagar = totalAmount,
            tarjeta = getCardType(cardNumber),
            numeroTarjeta = "XXXX-XXXX-XXXX-${cardNumber.takeLast(4)}",
            productos = detallesVenta
        )

        // Ejecutar la llamada a la API en un hilo de fondo
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val service = CheckoutService()
                val response = service.registrarVenta(ventaRequest)

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    handlePaymentResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toasty.error(
                        requireContext(),
                        "Error al procesar el pago: ${e.message}",
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // FUNCIONES AUXILIARES

    private fun getCurrentClientId(): Int {
        return try {
            val user = sessionManager.getUserDetails()

            println("🔍 Buscando ID Cliente para usuario: ${user?.usuario}")
            println("📋 Todos los datos del usuario: $user")

            // Intentar obtener idCliente del usuario
            val idCliente = user?.idCliente

            if (idCliente != null) {
                println("✅ ID Cliente encontrado: $idCliente")
                idCliente
            } else {
                println("⚠️ idCliente es null, usando mapeo temporal")
                // Mapeo temporal mientras la API se actualiza
                obtenerIdClienteTemporal(user?.usuario)
            }
        } catch (e: Exception) {
            println("❌ Error al obtener ID Cliente: ${e.message}")
            obtenerIdClienteTemporal(null)
        }
    }

    /**
     * Función temporal para obtener el ID del cliente
     * Mientras la API se actualiza para devolver idCliente en el login
     */
    private fun obtenerIdClienteTemporal(usuario: String?): Int {
        // Mapeo temporal de usuarios a IDs
        return when (usuario) {
            "admin" -> 1
            "cliente1" -> 2
            "cliente2" -> 3
            "camilo" -> 4
            "usuario1" -> 5
            "usuario2" -> 6
            else -> {
                println("⚠️ Usuario no mapeado: $usuario, usando ID por defecto: 1")
                1 // Default al ID 1
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnConfirmPayment.isEnabled = !isLoading
    }

    private fun handlePaymentResponse(response: VentaResponse?) {
        if (response == null || response.codigo != "200") {
            val message = response?.mensaje ?: "Error de conexión o servidor desconocido."
            Toasty.error(requireContext(), "Fallo en el Pago: $message", Toasty.LENGTH_LONG).show()
        } else {
            // Éxito:
            Toasty.success(
                requireContext(),
                "¡Venta registrada! ID: ${response.resultado?.idVenta}",
                Toasty.LENGTH_LONG
            ).show()

            // Limpiar el carrito y navegar de vuelta a la lista de productos
            CartManager.clearCart()

            // Navegar de vuelta al fragmento de productos
            parentFragmentManager.popBackStack()
        }
    }

    private fun getCardType(cardNumber: String): String {
        return when (cardNumber.firstOrNull()) {
            '4' -> "Visa"
            '5' -> "Mastercard"
            '3' -> "Amex"
            else -> "Otro"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}