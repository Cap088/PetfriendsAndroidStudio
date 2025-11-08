package com.camilop.petfriendsapp_kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.camilop.petfriendsapp_kotlin.databinding.ActivityRegisterBinding
import com.camilop.petfriendsapp_kotlin.models.APIResponse
import com.camilop.petfriendsapp_kotlin.models.RegisterRequest
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        binding.btnRegistrarUsuario.setOnClickListener {
            registrarUsuario()
        }

        binding.btnVolverLogin.setOnClickListener {
            finish() // Cierra esta Activity y regresa a LoginActivity
        }
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun registrarUsuario() {
        // 1. EXTRAER LOS 5 CAMPOS NECESARIOS (sin Confirmar Contraseña)
        val nombre = binding.etRegNombre.text.toString().trim()
        val apellido = binding.etRegApellido.text.toString().trim()
        val usuario = binding.etRegUsuario.text.toString().trim()
        val contrasena = binding.etRegContrasena.text.toString().trim()
        val telefono = binding.etRegTelefono.text.toString().trim()

        // VALIDACIÓN BÁSICA (Asegurandose que los 5 campos estén llenos)
        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() ||
            contrasena.isEmpty() || telefono.isEmpty()) {
            Toasty.warning(this, "Completa todos los campos.", Toasty.LENGTH_SHORT).show()
            return
        }

        // Crear el objeto de la petición con los 5 campos
        val registerRequest = RegisterRequest(
            nombre = nombre,
            apellido = apellido,
            usuario = usuario,
            contrasena = contrasena,
            telefono = telefono
        )

        // Llamar a la API
        apiService.registro(registerRequest).enqueue(object : Callback<APIResponse> {

            override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse != null && apiResponse.codigo == "200") {
                        // REGISTRO EXITOSO
                        Toasty.success(this@RegisterActivity, apiResponse.mensaje, Toasty.LENGTH_LONG).show()
                        finish() // Vuelve a LoginActivity

                    } else {
                        // REGISTRO FALLIDO
                        val mensajeError = apiResponse?.mensaje ?: "Error en el registro."
                        Toasty.error(this@RegisterActivity, mensajeError, Toasty.LENGTH_LONG).show()
                    }
                } else {
                    Toasty.error(this@RegisterActivity, "Error en el servidor: ${response.code()}", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                Toasty.error(this@RegisterActivity, "Error de conexión: ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
}