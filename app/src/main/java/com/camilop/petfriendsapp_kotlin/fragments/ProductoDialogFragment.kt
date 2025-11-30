package com.camilop.petfriendsapp_kotlin.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.models.Product

class ProductoDialogFragment : DialogFragment() {

    private var producto: Product? = null
    private var onProductoGuardado: ((Product) -> Unit)? = null
    private var imagenUri: Uri? = null
    private lateinit var ivImagen: ImageView

    companion object {
        fun newInstance(producto: Product?, onProductoGuardado: (Product) -> Unit): ProductoDialogFragment {
            val fragment = ProductoDialogFragment()
            fragment.producto = producto
            fragment.onProductoGuardado = onProductoGuardado
            return fragment
        }
    }

    // Registrar el launcher para seleccionar imágenes
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            Glide.with(requireContext())
                .load(it)
                .placeholder(R.drawable.ic_placeholder)
                .into(ivImagen)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_producto, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoria)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        ivImagen = view.findViewById<ImageView>(R.id.ivImagenProducto)

        // llenar los campos con los datos existentes
        producto?.let {
            etNombre.setText(it.nombre)
            etDescripcion.setText(it.descripcion)
            etPrecio.setText(it.precio.toString())
            etCategoria.setText(it.categoria)
            etStock.setText(it.cantidad.toString())

            // Cargar imagen existente si hay una URL
            it.imagenes?.let { imagenUrl ->
                if (imagenUrl.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(imagenUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(ivImagen)
                }
            }
        }

        // Listener para seleccionar imagen
        ivImagen.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        builder.setView(view)
            .setTitle(if (producto == null) "Agregar Producto" else "Editar Producto")
            .setPositiveButton("Guardar") { dialog, which ->
                val nombre = etNombre.text.toString()
                val descripcion = etDescripcion.text.toString()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val categoria = etCategoria.text.toString()
                val cantidad = etStock.text.toString().toIntOrNull() ?: 0


                val imagenes = if (imagenUri != null) {

                    imagenUri.toString()
                } else {
                    producto?.imagenes ?: ""
                }

                val productoActualizado = Product(
                    idProducto = producto?.idProducto ?: 0,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    categoria = categoria,
                    cantidad = cantidad,
                    porcentajeIva = 12.0,
                    imagenes = imagenes
                )

                onProductoGuardado?.invoke(productoActualizado)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

        return builder.create()
    }
}