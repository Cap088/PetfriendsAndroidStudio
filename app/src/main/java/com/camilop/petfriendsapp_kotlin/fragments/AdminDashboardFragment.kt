package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.camilop.petfriendsapp_kotlin.databinding.FragmentAdminDashboardBinding
import com.camilop.petfriendsapp_kotlin.viewmodels.AdminDashboardViewModel

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
        // ... (tu código existente de observadores) ...
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
                // Mostrar error si es necesario
                // Toasty.error(requireContext(), errorMessage, Toasty.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // 👇 NUEVOS CLICK LISTENERS PARA LAS TARJETAS

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

        // Reportes
        binding.cardReportes.setOnClickListener {
            // Navegar a la pantalla de reportes
            navigateToReports()
        }
    }

    // 👇 MÉTODOS DE NAVEGACIÓN (por implementar)
    private fun navigateToUserManagement() {
        // Por ahora mostraremos un mensaje, luego implementaremos la navegación real
        showMessage("Navegando a Gestión de Usuarios")
        // findNavController().navigate(R.id.action_adminDashboard_to_userManagementFragment)
    }

    private fun navigateToClientManagement() {
        showMessage("Navegando a Gestión de Clientes")
        // findNavController().navigate(R.id.action_adminDashboard_to_clientManagementFragment)
    }

    private fun navigateToSalesManagement() {
        showMessage("Navegando a Gestión de Ventas")
        // findNavController().navigate(R.id.action_adminDashboard_to_salesManagementFragment)
    }

    private fun navigateToReports() {
        showMessage("Navegando a Reportes")
        // findNavController().navigate(R.id.action_adminDashboard_to_reportsFragment)
    }

    private fun showMessage(message: String) {
        // Usar Toast o Snackbar para mostrar mensajes temporales
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}