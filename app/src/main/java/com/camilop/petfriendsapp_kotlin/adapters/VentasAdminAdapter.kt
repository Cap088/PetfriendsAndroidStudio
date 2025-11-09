package com.camilop.petfriendsapp_kotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.databinding.ItemVentaBinding
import com.camilop.petfriendsapp_kotlin.models.VentaAdmin

class VentasAdminAdapter(
    private val onViewDetails: (VentaAdmin) -> Unit,
    private val onPrintInvoice: (VentaAdmin) -> Unit
) : ListAdapter<VentaAdmin, VentasAdminAdapter.VentaViewHolder>(VentaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val binding = ItemVentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VentaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = getItem(position)
        holder.bind(venta)
    }

    inner class VentaViewHolder(private val binding: ItemVentaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(venta: VentaAdmin) {
            binding.tvSaleId.text = "Venta #${venta.idVenta}"
            binding.tvSaleDate.text = venta.fecha
            binding.tvClientName.text = "Cliente: ${venta.cliente_nombre} ${venta.cliente_apellido}"
            binding.tvSaleAmount.text = "$${String.format("%.2f", venta.valorPagar)}"
            binding.tvPaymentMethod.text = if (venta.tarjeta != null) {
                "Tarjeta •••• ${venta.numeroTarjeta ?: "N/A"}"
            } else {
                "Efectivo"
            }

            // Configurar estado
            when (venta.estado) {
                1 -> {
                    binding.tvSaleStatus.text = "Completada"
                    binding.tvSaleStatus.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.verde)
                    )
                }
                0 -> {
                    binding.tvSaleStatus.text = "Cancelada"
                    binding.tvSaleStatus.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.red_status)
                    )
                }
                else -> {
                    binding.tvSaleStatus.text = "Pendiente"
                    binding.tvSaleStatus.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.azul)
                    )
                }
            }

            // Listeners
            binding.btnViewDetails.setOnClickListener {
                onViewDetails(venta)
            }

            binding.btnPrintInvoice.setOnClickListener {
                onPrintInvoice(venta)
            }

            // Click en toda la tarjeta
            binding.root.setOnClickListener {
                onViewDetails(venta)
            }
        }
    }

    class VentaDiffCallback : DiffUtil.ItemCallback<VentaAdmin>() {
        override fun areItemsTheSame(oldItem: VentaAdmin, newItem: VentaAdmin): Boolean {
            return oldItem.idVenta == newItem.idVenta
        }

        override fun areContentsTheSame(oldItem: VentaAdmin, newItem: VentaAdmin): Boolean {
            return oldItem == newItem
        }
    }
}