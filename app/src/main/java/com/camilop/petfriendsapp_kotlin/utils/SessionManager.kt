package com.camilop.petfriendsapp_kotlin.utils

import android.content.Context
import android.content.SharedPreferences
import com.camilop.petfriendsapp_kotlin.models.User
import com.google.gson.Gson

class SessionManager(context: Context) {

    // Constantes
    private val PREFS_NAME = "MyPrefs"
    private val IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USER_DATA = "userData"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()
    private val gson = Gson()

    /**
     * Guarda el estado de la sesión y los datos completos del usuario usando GSON
     */
    fun createLoginSession(user: User) {
        // 👇 DEBUGGING
        println("💾 createLoginSession()")
        println("👤 Guardando usuario: ${user.usuario}")
        println("🎯 Guardando rol: ${user.rol}")
        println("📝 Todo el objeto user: $user")

        // Convertir el objeto User a JSON y guardarlo
        val userJson = gson.toJson(user)
        editor.putString(KEY_USER_DATA, userJson)
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()

        println("✅ Sesión guardada exitosamente con GSON")
    }

    /**
     * Obtiene el objeto User completo a partir del JSON guardado
     */
    fun getUserDetails(): User? {
        if (!isLoggedIn()) {
            return null
        }

        val userJson = prefs.getString(KEY_USER_DATA, null)

        // 👇 DEBUGGING
        println("🔍 getUserDetails()")
        println("📦 JSON recuperado: $userJson")

        return if (userJson != null) {
            try {
                val user = gson.fromJson(userJson, User::class.java)
                println("👤 Usuario recuperado: ${user.usuario}")
                println("🎯 Rol recuperado: ${user.rol}")
                user
            } catch (e: Exception) {
                println("❌ Error al parsear JSON: ${e.message}")
                null
            }
        } else {
            println("❌ No hay datos de usuario guardados")
            null
        }
    }

    /**
     * Verifica si el usuario ha iniciado sesión
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    /**
     * Verifica si el usuario es administrador
     */
    fun isAdmin(): Boolean {
        val user = getUserDetails()

        // 👇 DEBUGGING MEJORADO
        println("🔍 DEBUG SessionManager - isAdmin()")
        println("👤 Usuario: ${user?.usuario}")
        println("🎯 Rol: ${user?.rol}")
        println("📱 Teléfono: ${user?.telefono}")

        val esAdmin = user?.rol == "admin"
        println("✅ ES ADMIN: $esAdmin")

        return esAdmin
    }

    /**
     * Cierra la sesión (borra todos los datos guardados)
     */
    fun logout() {
        // 👇 DEBUGGING
        println("🚪 Cerrando sesión...")

        editor.clear()
        editor.apply()

        println("✅ Sesión cerrada exitosamente")
    }

    /**
     * Para debugging - muestra info completa del usuario
     */
    fun logUserInfo() {
        val user = getUserDetails()
        println("=== DEBUG USER INFO ===")
        println("👤 USUARIO: ${user?.usuario}")
        println("🎯 ROL: ${user?.rol}")
        println("📞 TELÉFONO: ${user?.telefono}")
        println("🔐 ES ADMIN: ${isAdmin()}")
        println("=======================")
    }
}