package com.camilop.petfriendsapp_kotlin.fragments

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

    // MOCK: Asumimos un ID de cliente (debe obtenerse del estado de sesión)
    private val MOCK_CLIENT_ID = 1

    // Tasa del 16%
    private val IVA_RATE = 0.16

    companion object {
        private const val ARG_TOTAL_AMOUNT = "total_amount"

        // Metodo de fábrica para crear la instancia con argumentos
        fun newInstance(totalAmount: Double): PagosFragment { // SOLO UN ARGUMENTO
            val fragment = PagosFragment()
            val args = Bundle().apply {
                putDouble(ARG_TOTAL_AMOUNT, totalAmount)
            }
            fragment.arguments = args
            return fragment
        }
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
        // Validación simple de campos (ejemplo)
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

        // Llamamos a la lógica de pago
        processPayment(cardNumber)
    }

    private fun processPayment(cardNumber: String) {

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

        // Crear la solicitud VentaRequest
        val ventaRequest = VentaRequest(
            idCliente = MOCK_CLIENT_ID,
            valorPagar = totalAmount,
            tarjeta = getCardType(cardNumber), // Lógica para detectar tipo (Mock)
            numeroTarjeta = "XXXX-XXXX-XXXX-${cardNumber.takeLast(4)}", // Solo los últimos 4 dígitos
            productos = detallesVenta
        )

        // Ejecutar la llamada a la API en un hilo de fondo
        GlobalScope.launch(Dispatchers.IO) {
            val service = CheckoutService()
            val response = service.registrarVenta(ventaRequest)

            withContext(Dispatchers.Main) {
                setLoading(false)
                handlePaymentResponse(response)
            }
        }
    }

    // FUNCIONES AUXILIARES

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

            // Navegar de vuelta al fragmento de productos (vuelve al carrito, que se auto-actualizará)
            parentFragmentManager.popBackStack()
        }
    }

    private fun getCardType(cardNumber: String): String {
        // Lógica Mock: Determinar tipo de tarjeta por el primer dígito
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
