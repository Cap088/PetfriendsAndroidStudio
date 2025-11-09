package com.camilop.petfriendsapp_kotlin.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.adapters.ClientAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentClientManagementBinding
import com.camilop.petfriendsapp_kotlin.models.Cliente
import com.camilop.petfriendsapp_kotlin.models.ClienteUpdateRequest
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import com.camilop.petfriendsapp_kotlin.viewmodels.ClientManagementViewModel
import es.dmoral.toasty.Toasty

class ClientManagementFragment : Fragment() {

    private lateinit var binding: FragmentClientManagementBinding
    private val viewModel: ClientManagementViewModel by viewModels()
    private lateinit var clientAdapter: ClientAdapter
    private var searchRunnable: Runnable = Runnable { }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        viewModel.loadClients()
    }

    private fun setupRecyclerView() {
        clientAdapter = ClientAdapter(
            onEditClient = { client ->
                showEditClientDialog(client)
            },
            onViewSales = { client ->
                showClientSales(client)
            }
        )

        binding.rvClients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clientAdapter
        }
    }

    // Configuración de observadores
    private fun setupObservers() {
        viewModel.clients.observe(viewLifecycleOwner) { clients ->
            clientAdapter.submitList(clients)
            binding.tvEmptyState.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                if (it.isNotBlank()) {
                    showMessage("Error: $it")
                }
            }
        }
    }

    // Configuración de listeners
    private fun setupClickListeners() {
        binding.etSearchClient.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.etSearchClient.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etSearchClient.removeCallbacks(searchRunnable)
                searchRunnable = Runnable { performSearch() }
                binding.etSearchClient.postDelayed(searchRunnable, 500)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch() {
        val query = binding.etSearchClient.text.toString().trim()
        hideKeyboard()
        viewModel.searchClients(query)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchClient.windowToken, 0)
    }

    private fun showEditClientDialog(client: Cliente) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_client, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etNombre = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNombre)
        val etApellido = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etApellido)
        val etcedula = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCedula)
        val etTelefono = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelefono)
        val etdireccion = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDireccion)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)

        // Llenar datos del cliente
        etNombre.setText(client.nombre)
        etApellido.setText(client.apellido)
        etcedula.setText(client.cedula)
        etTelefono.setText(client.telefono)
        etdireccion.setText(client.direccion)

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnGuardar.setOnClickListener {
            guardarCambiosCliente(client, dialogView, dialog)
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // Función para guardar cambios en el cliente
    private fun guardarCambiosCliente(
        clienteOriginal: Cliente,
        dialogView: View,
        dialog: android.app.AlertDialog
    ) {
        val etNombre = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNombre)
        val etApellido = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etApellido)
        val etCedula = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCedula)
        val etTelefono = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelefono)
        val etDireccion = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDireccion)

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val cedula = etCedula.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.error = "El nombre es requerido"
            return
        }
        if (apellido.isEmpty()) {
            etApellido.error = "El apellido es requerido"
            return
        }
        if (cedula.isEmpty()) {
            etCedula.error = "La cédula es requerida"
            return
        }
        if (telefono.isEmpty()) {
            etTelefono.error = "El teléfono es requerido"
            return
        }
        if (direccion.isEmpty()) {
            etDireccion.error = "La dirección es requerida"
            return
        }

        // Mostrar loading
        val progressDialog = android.app.ProgressDialog(requireContext()).apply {
            setMessage("Actualizando cliente...")
            setCancelable(false)
            show()
        }

        // Crear el request con los datos actualizados
        val request = ClienteUpdateRequest(
            nombre = nombre,
            apellido = apellido,
            cedula = cedula,
            telefono = telefono,
            direccion = direccion
        )

        // Llamar a la API para actualizar
        RetrofitClient.apiService.actualizarCliente(clienteOriginal.idCliente, request)
            .enqueue(object : retrofit2.Callback<com.camilop.petfriendsapp_kotlin.models.BaseResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.BaseResponse>,
                    response: retrofit2.Response<com.camilop.petfriendsapp_kotlin.models.BaseResponse>
                ) {
                    progressDialog.dismiss()

                    if (response.isSuccessful) {
                        response.body()?.let { apiResponse ->
                            if (apiResponse.codigo == "200") {
                                Toasty.success(requireContext(), "Cliente actualizado correctamente", Toasty.LENGTH_SHORT).show()
                                dialog.dismiss()
                                // Recargar la lista de clientes
                                viewModel.loadClients()
                            } else {
                                Toasty.error(requireContext(), "Error: ${apiResponse.mensaje}", Toasty.LENGTH_LONG).show()
                            }
                        } ?: run {
                            Toasty.error(requireContext(), "Error: Respuesta vacía del servidor", Toasty.LENGTH_LONG).show()
                        }
                    } else {
                        Toasty.error(requireContext(), "Error del servidor: ${response.code()}", Toasty.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.camilop.petfriendsapp_kotlin.models.BaseResponse>,
                    t: Throwable
                ) {
                    progressDialog.dismiss()
                    Toasty.error(requireContext(), "Error de conexión: ${t.message}", Toasty.LENGTH_LONG).show()
                }
            })
    }

    private fun showClientSales(client: Cliente) {
        showMessage("Ver ventas de: ${client.nombre} ${client.apellido}")
        // Aquí puedes implementar la navegación a las ventas del cliente
    }

    private fun showMessage(message: String) {
        Toasty.info(requireContext(), message, Toasty.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        binding.etSearchClient.removeCallbacks(searchRunnable)
        super.onDestroyView()
    }
}