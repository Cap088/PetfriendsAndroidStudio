package com.camilop.petfriendsapp_kotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.databinding.ItemProductoVentaBinding
import com.camilop.petfriendsapp_kotlin.models.DetalleVenta
import java.text.NumberFormat
import java.util.Locale

class VentaDetalleAdapter : ListAdapter<DetalleVenta, VentaDetalleAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    private val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoVentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = getItem(position)
        holder.bind(producto)
    }

    inner class ProductoViewHolder(private val binding: ItemProductoVentaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: DetalleVenta) {
            binding.tvProductName.text = producto.nombreProducto
            binding.tvCantidad.text = "Cantidad: ${producto.cantidad}"
            binding.tvPrecioUnitario.text = "Precio: ${format.format(producto.precioUnitario)}"
            binding.tvSubtotal.text = "Subtotal: ${format.format(producto.subtotal)}"
            binding.tvDescuento.text = "Descuento: ${format.format(producto.descuento)}"
            binding.tvIva.text = "IVA: ${format.format(producto.iva)}"
            binding.tvTotal.text = "Total: ${format.format(producto.totalPagar)}"
        }
    }

    class ProductoDiffCallback : DiffUtil.ItemCallback<DetalleVenta>() {
        override fun areItemsTheSame(oldItem: DetalleVenta, newItem: DetalleVenta): Boolean {
            return oldItem.idDetalleVenta == newItem.idDetalleVenta
        }

        override fun areContentsTheSame(oldItem: DetalleVenta, newItem: DetalleVenta): Boolean {
            return oldItem == newItem
        }
    }
}