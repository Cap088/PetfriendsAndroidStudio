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
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.adapters.UserAdapter
import com.camilop.petfriendsapp_kotlin.databinding.FragmentUserManagementBinding
import com.camilop.petfriendsapp_kotlin.models.UsuarioAdmin
import com.camilop.petfriendsapp_kotlin.models.UsuarioAdminListResponse
import com.camilop.petfriendsapp_kotlin.models.UsuarioUpdateRequest
import com.camilop.petfriendsapp_kotlin.network.RetrofitClient
import com.camilop.petfriendsapp_kotlin.viewmodels.UserManagementViewModel
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.models.User

class UserManagementFragment : Fragment() {

    private lateinit var binding: FragmentUserManagementBinding
    private val viewModel: UserManagementViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        viewModel.loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onEditUser = { usuario ->
                showEditUserDialog(usuario)
            }
        )

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.submitList(users)

            // Mostrar/ocultar empty state
            binding.tvEmptyState.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
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

    private fun setupClickListeners() {
        // Configurar búsqueda cuando se presiona el botón de búsqueda
        binding.etSearchUser.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Configurar búsqueda en tiempo real (opcional)
        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Búsqueda en tiempo real después de 500ms
                binding.etSearchUser.removeCallbacks(searchRunnable)
                searchRunnable = Runnable {
                    performSearch()
                }
                binding.etSearchUser.postDelayed(searchRunnable, 500)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch() {
        val query = binding.etSearchUser.text.toString().trim()
        hideKeyboard()
        viewModel.searchUsers(query)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchUser.windowToken, 0)
    }

    companion object {
        private var searchRunnable: Runnable = Runnable { }
    }

    private fun showEditUserDialog(user: UsuarioAdmin) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_user, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Referencias a los views
        val etNombre = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNombre)
        val etApellido = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etApellido)
        val etUsuario = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUsuario)
        val etTelefono = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelefono)
        val etRol = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRol)
        val switchEstado = dialogView.findViewById<Switch>(R.id.switchEstado)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)

        // Llenar datos del usuario - ESTO DEBERÍA FUNCIONAR AHORA
        etNombre.setText(user.nombre)
        etApellido.setText(user.apellido)
        etUsuario.setText(user.usuario)
        etTelefono.setText(user.telefono)
        etRol.setText(user.rol)
        switchEstado.isChecked = user.estado == 1

        // Listeners
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            guardarCambiosUsuario(user, dialogView, dialog)
        }

        dialog.show()

        // Ajustar tamaño del diálogo
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun guardarCambiosUsuario(
        usuarioOriginal: UsuarioAdmin,
        dialogView: View,
        dialog: android.app.AlertDialog
    ) {
        val etNombre = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNombre)
        val etApellido = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etApellido)
        val etTelefono = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelefono)
        val etRol = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRol)
        val switchEstado = dialogView.findViewById<Switch>(R.id.switchEstado)

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val rol = etRol.text.toString().trim()
        val estado = if (switchEstado.isChecked) 1 else 0

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.error = "El nombre es requerido"
            return
        }

        if (apellido.isEmpty()) {
            etApellido.error = "El apellido es requerido"
            return
        }

        if (rol.isEmpty()) {
            etRol.error = "El rol es requerido"
            return
        }

        // Mostrar loading
        val progressDialog = android.app.ProgressDialog(requireContext()).apply {
            setMessage("Actualizando usuario...")
            setCancelable(false)
            show()
        }

        // Llamar a la API para actualizar
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = UsuarioUpdateRequest(
                    nombre = nombre,
                    apellido = apellido,
                    telefono = telefono,
                    rol = rol,
                    estado = estado == 1
                )

                val call = RetrofitClient.apiService.actualizarUsuario(usuarioOriginal.idUsuario, request)
                val response = call.execute()

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!

                        if (apiResponse.codigo == "200") {
                            Toasty.success(requireContext(), "Usuario actualizado correctamente", Toasty.LENGTH_SHORT).show()
                            dialog.dismiss()
                            // Recargar la lista
                            viewModel.loadUsers()
                        } else {
                            Toasty.error(requireContext(), "Error: ${apiResponse.mensaje}", Toasty.LENGTH_LONG).show()
                        }
                    } else {
                        Toasty.error(requireContext(), "Error al actualizar usuario", Toasty.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toasty.error(requireContext(), "Error: ${e.message}", Toasty.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}