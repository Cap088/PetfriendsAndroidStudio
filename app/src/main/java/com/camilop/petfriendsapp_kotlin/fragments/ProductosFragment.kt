package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.adapters.ProductAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentProductosBinding
import com.camilop.petfriendsapp_kotlin.models.Product
import com.camilop.petfriendsapp_kotlin.models.ProductListResponse
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicia la obtención de datos de la API
        getProductsFromApi()
    }


    //Realiza la llamada asíncrona a la API REST para obtener la lista de productos.

    private fun getProductsFromApi() {
        // La llamada espera el objeto contenedor ProductListResponse
        RetrofitClient.apiService.getProducts().enqueue(object : Callback<ProductListResponse> {

            override fun onResponse(
                call: Call<ProductListResponse>,
                response: Response<ProductListResponse>
            ) {

                if (response.isSuccessful) {
                    val productListResponse = response.body()

                    if (productListResponse?.codigo == "200") {

                        // Se Extrae la lista REAL de productos del campo 'resultado'
                        productListResponse.resultado.let { products ->
                            if (products.isNotEmpty()) {
                                setupRecyclerView(products)
                            } else {
                                Toasty.info(
                                    requireContext(),
                                    "No hay productos activos para mostrar.",
                                    Toasty.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        // Manejo de mensajes de éxito sin datos o errores controlados por la API
                        val mensaje = productListResponse?.mensaje ?: "Respuesta de API inválida."
                        Toasty.warning(requireContext(), mensaje, Toasty.LENGTH_LONG).show()
                    }
                } else {
                    // Manejo de errores HTTP (404, 500, etc.)
                    Toasty.error(
                        requireContext(),
                        "Error del servidor: Código ${response.code()}",
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }

            // Se ejecuta si hay un fallo en la conexión de red
            override fun onFailure(call: Call<ProductListResponse>, t: Throwable) {
                // Muestra un mensaje de fallo de conexión
                Toasty.error(
                    requireContext(),
                    "Fallo de conexión: ${t.message}",
                    Toasty.LENGTH_LONG
                ).show()
            }
        })
    }

    //Configura el RecyclerView con la lista de productos recibida.

    private fun setupRecyclerView(products: List<Product>) {

        binding.rvProducts.layoutManager = LinearLayoutManager(context)

        val adapter = ProductAdapter(requireContext(), products)
        binding.rvProducts.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}