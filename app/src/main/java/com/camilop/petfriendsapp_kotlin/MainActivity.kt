package com.camilop.petfriendsapp_kotlin

import com.camilop.petfriendsapp_kotlin.fragments.FacturacionFragment
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.camilop.petfriendsapp_kotlin.fragments.CartFragment
import com.camilop.petfriendsapp_kotlin.databinding.ActivityMainBinding
import com.camilop.petfriendsapp_kotlin.fragments.AdminDashboardFragment
import com.camilop.petfriendsapp_kotlin.fragments.ProductosFragment
import com.camilop.petfriendsapp_kotlin.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import com.camilop.petfriendsapp_kotlin.fragments.ContactFragment
import com.camilop.petfriendsapp_kotlin.fragments.VentasFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var drawerLayout: DrawerLayout

    // Configuración inicial de la actividad.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(applicationContext)
        drawerLayout = binding.drawerLayout

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setLogo(R.drawable.logo)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        displayUserDetails()
        setupAdminMenuVisibility()

        if (savedInstanceState == null) {
            loadFragment(ProductosFragment())
        }
    }

    // Muestra u oculta el menú de administración según el rol del usuario.
    private fun setupAdminMenuVisibility() {
        val navView = binding.navView
        val menu = navView.menu

        // Buscar el item que contiene "Panel de Control"
        var adminItem: MenuItem? = null

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.hasSubMenu()) {
                val subMenu = item.subMenu
                // USAR safe call (?.) para subMenu
                subMenu?.let { safeSubMenu ->
                    for (j in 0 until safeSubMenu.size()) {
                        val subItem = safeSubMenu.getItem(j)
                        if (subItem.itemId == R.id.nav_admin_dashboard) {
                            adminItem = item // Este es el item padre que contiene el admin
                            println("Encontrado item admin: ${subItem.title}")
                            break
                        }
                    }
                }
            }
        }

        val esAdmin = sessionManager.isAdmin()
        println("Usuario es admin: $esAdmin")

        adminItem?.isVisible = esAdmin

        if (esAdmin && adminItem != null) {
            println("MENÚ ADMIN VISIBLE")
            Toast.makeText(this, "Modo Administrador Activado", Toast.LENGTH_LONG).show()
        } else if (adminItem == null) {
            println("ITEM ADMIN NO ENCONTRADO")
            Toast.makeText(this, "Error: Item admin no encontrado", Toast.LENGTH_LONG).show()
        } else {
            println("USUARIO NO ES ADMIN - Menú oculto")
        }
    }
    // Actualiza el menú de administración según el rol del usuario.
    override fun onResume() {
        super.onResume()
        setupAdminMenuVisibility()
    }

    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Muestra los detalles del usuario en la vista de bienvenida.
    private fun displayUserDetails() {

        val user = sessionManager.getUserDetails()

        if (user != null) {
            binding.tvBienvenida.text = "¡Bienvenido, ${user.nombre} ${user.apellido}!"
        } else {
            binding.tvBienvenida.text = "¡Bienvenido! Error al cargar datos."
            logoutUser()
        }
    }


    // Maneja el clic en los ítems del menú lateral (Navigation Drawer).

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> loadFragment(ProductosFragment())
            R.id.nav_productos -> loadFragment(ProductosFragment())
            R.id.nav_contactanos -> loadFragment(ContactFragment())
            R.id.nav_carrito -> loadFragment(CartFragment())
            R.id.nav_compras -> loadFragment(VentasFragment())
            R.id.nav_facturacion -> loadFragment(FacturacionFragment())
            //Menu solo para administrador
            R.id.nav_admin_dashboard -> {
                if (sessionManager.isAdmin()) {
                    loadFragment(AdminDashboardFragment())
                } else {
                    Toast.makeText(this, "Acceso no autorizado", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.nav_logout_drawer -> logoutUser()
        }

        drawerLayout.closeDrawer(binding.navView)
        return true
    }
    // Cierra la sesión del usuario y redirige a la pantalla de inicio de sesión.
    private fun logoutUser() {

        sessionManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}