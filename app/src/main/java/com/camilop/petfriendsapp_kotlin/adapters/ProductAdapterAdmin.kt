package com.camilop.petfriendsapp_kotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.databinding.ItemProductAdminBinding
import com.camilop.petfriendsapp_kotlin.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapterAdmin(
    private val context: Context,
    private val products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapterAdmin.ProductAdminViewHolder>() {

    inner class ProductAdminViewHolder(val binding: ItemProductAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdminViewHolder {
        val binding = ItemProductAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductAdminViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductAdminViewHolder, position: Int) {
        val currentProduct = products[position]

        // Formato de moneda
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // ASIGNACIÓN DE DATOS
        holder.binding.tvProductName.text = currentProduct.nombre
        holder.binding.tvProductPrice.text = format.format(currentProduct.precio)
        holder.binding.tvProductCategory.text = currentProduct.categoria
        holder.binding.tvProductDescription.text = currentProduct.descripcion
        holder.binding.tvProductStock.text = "Stock: ${currentProduct.cantidad}"

        // CARGAR IMAGEN
        val imageName = "producto_${currentProduct.idProducto}"
        val imageResourceId = context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        )

        if (imageResourceId != 0) {
            holder.binding.imgProducto.setImageResource(imageResourceId)
        } else {
            holder.binding.imgProducto.setImageResource(R.drawable.ic_placeholder)
        }

        // BOTONES DE ADMINISTRACIÓN
        holder.binding.btnEditProduct.setOnClickListener {
            onEditClick(currentProduct)
        }

        holder.binding.btnDeleteProduct.setOnClickListener {
            onDeleteClick(currentProduct)
        }
    }

    override fun getItemCount() = products.size
}