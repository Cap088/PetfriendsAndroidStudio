package com.camilop.petfriendsapp_kotlin.Facturas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.databinding.ItemProductoFacturaBinding
import com.camilop.petfriendsapp_kotlin.models.DetalleVenta

class ProductosFacturaAdapter(private val productos: List<DetalleVenta>) :
    RecyclerView.Adapter<ProductosFacturaAdapter.ViewHolder>() {

    // ViewHolder class
    class ViewHolder(val binding: ItemProductoFacturaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoFacturaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]

        with(holder.binding) {
            tvProductoNombre.text = producto.nombreProducto
            tvProductoCantidad.text = "Cantidad: ${producto.cantidad}"
            tvProductoPrecioUnitario.text = "Precio: $${String.format("%.2f", producto.precioUnitario)}"
            tvProductoSubtotal.text = "Subtotal: $${String.format("%.2f", producto.subtotal)}"

            // Mostrar descuento solo si es mayor a 0
            if (producto.descuento > 0) {
                tvProductoDescuento.visibility = View.VISIBLE
                tvProductoDescuento.text = "Descuento: -$${String.format("%.2f", producto.descuento)}"
            } else {
                tvProductoDescuento.visibility = View.GONE
            }

            // Mostrar IVA
            tvProductoIva.text = "IVA: $${String.format("%.2f", producto.iva)}"

            // Total del producto
            tvProductoTotal.text = "Total: $${String.format("%.2f", producto.totalPagar)}"
        }
    }

    override fun getItemCount(): Int = productos.size
}