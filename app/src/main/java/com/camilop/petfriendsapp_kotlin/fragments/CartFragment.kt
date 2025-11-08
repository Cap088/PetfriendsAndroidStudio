package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.adapters.CartAdapter
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.databinding.FragmentCartBinding
import java.util.Map
import com.camilop.petfriendsapp_kotlin.models.Product
import com.camilop.petfriendsapp_kotlin.utils.CartManager
import es.dmoral.toasty.Toasty
import java.text.NumberFormat
import java.util.Locale


class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRecyclerView()

        //boton de checkout para ir a la pantalla de pago
        binding.btnCheckout.setOnClickListener {
            navigateToPaymentScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        setupCartRecyclerView()
    }

    //funcion para configurar el recyclerview del carrito
    private fun setupCartRecyclerView() {

        val cartItems = CartManager.getCartItems() as List<Map.Entry<Product, Int>>
        val mutableCartItems = cartItems.toMutableList()

        if (mutableCartItems.isEmpty()) {
            binding.rvCartItems.visibility = View.GONE
            binding.layoutSummary.visibility = View.GONE
            binding.tvEmptyCartMessage.visibility = View.VISIBLE

            binding.btnCheckout.isEnabled = false
        } else {
            binding.rvCartItems.visibility = View.VISIBLE
            binding.layoutSummary.visibility = View.VISIBLE
            binding.tvEmptyCartMessage.visibility = View.GONE
            binding.btnCheckout.isEnabled = true // Habilita el botón

            binding.rvCartItems.layoutManager = LinearLayoutManager(context)

            binding.rvCartItems.adapter = CartAdapter(
                requireContext(),
                mutableCartItems,
                onCartUpdated = {
                    updateCartSummary()
                    if (CartManager.getCartItems().isEmpty()) {
                        setupCartRecyclerView()
                    }
                }
            )
        }

        updateCartSummary()
    }

    private fun updateCartSummary() {
        val subtotal = CartManager.getCartSubtotal()
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        binding.tvCartSubtotal.text = format.format(subtotal)
    }

    // LÓGICA DE NAVEGACIÓN A LA PANTALLA DE PAGO

    private fun navigateToPaymentScreen() {
        val totalAmount = CartManager.getCartSubtotal()

        if (totalAmount <= 0.0) {
            Toasty.warning(requireContext(), "El total debe ser mayor a cero para pagar.", Toasty.LENGTH_SHORT).show()
            return
        }

        // Llama a newInstance con UN SOLO argumento (totalAmount)
        val pagosFragment = PagosFragment.newInstance(totalAmount)

        // Redirige al Fragmento de Pago
        parentFragmentManager.beginTransaction()
            // ID del Contenedor Principal: Debe coincidir con el ID en activity_main.xml
            .replace(R.id.content_frame, pagosFragment)
            .addToBackStack(null) // Permite usar el botón de atrás para volver al carrito
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
