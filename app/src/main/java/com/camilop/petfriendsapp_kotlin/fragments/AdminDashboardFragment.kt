package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.camilop.petfriendsapp_kotlin.databinding.FragmentAdminDashboardBinding
import com.camilop.petfriendsapp_kotlin.viewmodels.AdminDashboardViewModel
import com.camilop.petfriendsapp_kotlin.R

class AdminDashboardFragment : Fragment() {

    private lateinit var binding: FragmentAdminDashboardBinding
    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        viewModel.loadDashboardData()
    }

    private fun setupObservers() {

        viewModel.totalUsuarios.observe(viewLifecycleOwner) { total ->
            binding.tvTotalUsuarios.text = total.toString()
        }

        viewModel.totalClientes.observe(viewLifecycleOwner) { total ->
            binding.tvTotalClientes.text = total.toString()
        }

        viewModel.totalVentas.observe(viewLifecycleOwner) { total ->
            binding.tvTotalVentas.text = total.toString()
        }

        viewModel.totalIngresos.observe(viewLifecycleOwner) { ingresos ->
            binding.tvTotalIngresos.text = "$${String.format("%.2f", ingresos)}"
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {

            }
        }
    }

    private fun setupClickListeners() {
        // NUEVOS CLICK LISTENERS PARA LAS TARJETAS

        // Gestión de Usuarios
        binding.cardGestionUsuarios.setOnClickListener {
            // Navegar a la pantalla de gestión de usuarios
            navigateToUserManagement()
        }

        // Gestión de Clientes
        binding.cardGestionClientes.setOnClickListener {
            // Navegar a la pantalla de gestión de clientes
            navigateToClientManagement()
        }

        // Gestión de Ventas
        binding.cardGestionVentas.setOnClickListener {
            // Navegar a la pantalla de gestión de ventas
            navigateToSalesManagement()
        }

        // Gestion de productos
        binding.cardGestionProductos.setOnClickListener {
            // Navegar a la pantalla de gestion de productos
            navigateToGestionProductos()
        }
    }

    // MÉTODOS DE NAVEGACIÓN
    private fun navigateToUserManagement() {

        val userManagementFragment = UserManagementFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, userManagementFragment)
            .addToBackStack("user_management")
            .commit()
    }

    private fun navigateToClientManagement() {

        val clientManagementFragment = ClientManagementFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, clientManagementFragment)
            .addToBackStack("client_management")
            .commit()
    }

    private fun navigateToSalesManagement() {

        val ventasAdminFragment = VentasAdminFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, ventasAdminFragment)
            .addToBackStack("ventas_admin")
            .commit()
    }

    private fun navigateToGestionProductos() {

        val gestionProductosFragment = GestionProductosFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, gestionProductosFragment)
            .addToBackStack("gestion_productos")
            .commit()
    }
}