package com.camilop.petfriendsapp_kotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.databinding.ItemProductBinding
import com.camilop.petfriendsapp_kotlin.models.Product
import com.camilop.petfriendsapp_kotlin.utils.CartManager
import es.dmoral.toasty.Toasty
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val context: Context,
    private val products: List<Product>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = products[position]

        // Formato de moneda
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // ASIGNACIÓN DE DATOS
        holder.binding.tvProductName.text = currentProduct.nombre
        holder.binding.tvProductPrice.text = format.format(currentProduct.precio)
        holder.binding.tvProductCategory.text = currentProduct.categoria
        holder.binding.tvProductDescription.text = currentProduct.descripcion


        // CARGAR DE IMAGEN (Asumiendo que las imágenes se llaman producto_ID)
        val imageName = "producto_${currentProduct.idProducto}"
        val imageResourceId = context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        )

        if (imageResourceId != 0) {
            holder.binding.imgProducto.setImageResource(imageResourceId)
        } else {
            // Placeholder si la imagen no se encuentra
            holder.binding.imgProducto.setImageResource(R.drawable.ic_launcher_background)
        }


        // FUNCIONALIDAD DEL BOTÓN "AGREGAR AL CARRITO"
        holder.binding.btnAddCart.setOnClickListener {
            // 🌟 LLAMADA CLAVE: Usar el CartManager para añadir el producto 🌟
            CartManager.addItem(currentProduct)

            // Notificación al usuario
            Toasty.success(
                context,
                "Agregado al carrito: ${currentProduct.nombre}",
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = products.size
}