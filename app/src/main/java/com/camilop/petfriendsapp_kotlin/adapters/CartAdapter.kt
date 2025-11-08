package com.camilop.petfriendsapp_kotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.databinding.ItemProductCartBinding
import com.camilop.petfriendsapp_kotlin.models.Product
import com.camilop.petfriendsapp_kotlin.utils.CartManager
import es.dmoral.toasty.Toasty
import java.text.NumberFormat
import java.util.Locale
import java.util.Map

class CartAdapter(
    private val context: Context,
    private val cartEntries: MutableList<Map.Entry<Product, Int>>,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemProductCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemProductCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {

        val cartEntry = cartEntries[position]
        val currentProduct = cartEntry.key
        val currentQuantity = cartEntry.value

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // ASIGNACIÓN DE DATOS
        holder.binding.tvCartProductName.text = currentProduct.nombre
        holder.binding.tvCartProductPrice.text = format.format(currentProduct.precio * currentQuantity) // Precio * Cantidad
        holder.binding.tvProductQuantity.text = currentQuantity.toString() // Mostrar la cantidad

        // Lógica de carga de imagen
        val imageName = "producto_${currentProduct.idProducto}"
        val imageResourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        if (imageResourceId != 0) {
            holder.binding.imgCartProduct.setImageResource(imageResourceId)
        } else {
            holder.binding.imgCartProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        // LÓGICA DE CONTROL DE CANTIDAD


        // Botón Sumar (+)
        holder.binding.btnAddOne.setOnClickListener {
            CartManager.addItem(currentProduct)
            onCartActionComplete(position)
        }

        // Botón Restar (-)
        holder.binding.btnRemoveOne.setOnClickListener {
            val wasRemovedCompletely = CartManager.removeOneItem(currentProduct)

            if (wasRemovedCompletely) {
                // Si la cantidad llega a cero, eliminamos la fila de la vista
                cartEntries.removeAt(position)
                notifyItemRemoved(position)
                Toasty.info(context, "Producto eliminado.", Toasty.LENGTH_SHORT).show()
            } else {
                // Si solo se restó una unidad, actualizamos la vista de la fila actual
                notifyItemChanged(position)
            }
            onCartUpdated() // Actualizar resumen
        }
    }

    // Función auxiliar para manejar la actualización de la vista
    private fun onCartActionComplete(position: Int) {
        // El CartManager ya tiene la nueva cantidad. Solo necesitamos actualizar esta fila
        notifyItemChanged(position)
        onCartUpdated() // Actualizar el subtotal
    }

    override fun getItemCount() = cartEntries.size
}