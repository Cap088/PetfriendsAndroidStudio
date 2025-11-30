package com.camilop.petfriendsapp_kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.camilop.petfriendsapp_kotlin.databinding.ActivityLoginBinding
import com.camilop.petfriendsapp_kotlin.models.APIResponse
import com.camilop.petfriendsapp_kotlin.models.LoginRequest
import com.camilop.petfriendsapp_kotlin.models.User
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import com.camilop.petfriendsapp_kotlin.utils.SessionManager
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  INICIALIZACIÓN Y VERIFICACIÓN DE SESIÓN
        sessionManager = SessionManager(applicationContext)

        // Si la sesión es activa, redirige inmediatamente a MainActivity
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // -----------------------------------------------------------

        // Configura ViewBinding (solo si la sesión no estaba activa)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Retrofit
        initRetrofit()

        // Configura listeners de botones
        binding.btnLogin.setOnClickListener {
            iniciarSesion()
        }

        binding.btnRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))

            Toasty.info(this, "Redirigiendo a Registro", Toasty.LENGTH_SHORT).show()
        }
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun iniciarSesion() {
        val usuario = binding.etUsuario.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toasty.warning(this, "Completa ambos campos.", Toasty.LENGTH_SHORT).show()
            return
        }

        val loginRequest = LoginRequest(usuario, contrasena)

        apiService.login(loginRequest).enqueue(object : Callback<APIResponse> {


            override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse != null && apiResponse.codigo == "200") {
                        // LOGIN EXITOSO
                        Toasty.success(this@LoginActivity, apiResponse.mensaje, Toasty.LENGTH_LONG).show()

                        //AGREGAR DEBUGGING AQUÍ
                        println("LOGIN EXITOSO")
                        println("Datos recibidos: ${apiResponse.resultado}")
                        println("Rol recibido: ${apiResponse.resultado?.rol}")

                        val user = apiResponse.resultado
                        if (user != null && user is User) {
                            sessionManager.createLoginSession(user)
                            println(" Usuario guardado en SessionManager")
                            println("Usuario: ${user.usuario}")
                            println("Rol guardado: ${user.rol}")
                        } else {
                            Log.w("LOGIN", "Login exitoso, pero el servidor no devolvió el objeto User en 'resultado'.")
                        }

                        // Redirigir a MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        val mensajeError = apiResponse?.mensaje ?: "Credenciales incorrectas."
                        Toasty.error(this@LoginActivity, mensajeError, Toasty.LENGTH_LONG).show()
                    }
                } else {
                    Toasty.error(this@LoginActivity, "Error en el servidor: ${response.code()}", Toasty.LENGTH_LONG).show()
                    Log.e("LOGIN", "HTTP Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                // Error de conexión (red, DNS, etc.)
                Toasty.error(this@LoginActivity, "Error de conexión: ${t.message}", Toasty.LENGTH_LONG).show()
                Log.e("LOGIN", "Fallo de conexión", t)
            }
        })
    }
}