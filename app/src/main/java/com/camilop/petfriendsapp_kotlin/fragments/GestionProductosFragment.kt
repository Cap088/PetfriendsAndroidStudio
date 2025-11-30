package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.adapters.ProductAdapterAdmin
import com.camilop.petfriendsapp_kotlin.databinding.FragmentGestionProductosBinding
import com.camilop.petfriendsapp_kotlin.viewmodels.GestionProductosViewModel
import com.camilop.petfriendsapp_kotlin.models.Product

class GestionProductosFragment : Fragment() {

    private lateinit var binding: FragmentGestionProductosBinding
    private val viewModel: GestionProductosViewModel by viewModels()
    private lateinit var productosAdapter: ProductAdapterAdmin

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGestionProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        viewModel.cargarProductos()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        productosAdapter = ProductAdapterAdmin(
            requireContext(),
            emptyList(),
            onEditClick = { producto ->
                // Editar producto
                mostrarDialogoProducto(producto)
            },
            onDeleteClick = { producto ->
                // Eliminar producto
                mostrarConfirmacionEliminar(producto)
            }
        )

        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productosAdapter
        }
    }

    private fun setupObservers() {
        viewModel.productos.observe(viewLifecycleOwner) { productos ->
            productosAdapter = ProductAdapterAdmin(
                requireContext(),
                productos,
                onEditClick = { producto ->
                    mostrarDialogoProducto(producto)
                },
                onDeleteClick = { producto ->
                    mostrarConfirmacionEliminar(producto)
                }
            )
            binding.rvProductos.adapter = productosAdapter
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvProductos.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAgregarProducto.setOnClickListener {
            mostrarDialogoProducto(null)
        }
    }

    private fun mostrarDialogoProducto(producto: Product?) {
        val dialog = ProductoDialogFragment.newInstance(producto) { productoActualizado ->
            if (producto == null) {
                viewModel.agregarProducto(productoActualizado)
            } else {
                viewModel.actualizarProducto(productoActualizado)
            }
        }
        dialog.show(requireActivity().supportFragmentManager, "ProductoDialogFragment")
    }

    private fun mostrarConfirmacionEliminar(producto: Product) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que quieres eliminar \"${producto.nombre}\"?")
            .setPositiveButton("Eliminar") { dialog, which ->
                viewModel.eliminarProducto(producto.idProducto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}