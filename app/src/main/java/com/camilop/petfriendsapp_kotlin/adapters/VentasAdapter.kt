package com.camilop.petfriendsapp_kotlin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.databinding.ItemSaleBinding
import com.camilop.petfriendsapp_kotlin.models.SaleHeader
import java.text.NumberFormat
import java.util.Locale

//Adaptador para mostrar una lista de ventas (SaleHeader).

class VentasAdapter(
    private val salesList: List<SaleHeader>,
    private val onItemClicked: ((SaleHeader) -> Unit)? = null // CAMBIO AQUÍ: Recibe SaleHeader completo
) : RecyclerView.Adapter<VentasAdapter.SaleViewHolder>() {

    // Formato de moneda para mostrar los precios
    private val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private var currentSalesList: List<SaleHeader> = salesList

    inner class SaleViewHolder(private val binding: ItemSaleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: SaleHeader) {
            binding.tvSaleId.text = "Venta #${sale.idVenta}"
            binding.tvSaleDate.text = "Fecha: ${sale.fecha}"
            binding.tvSaleTotal.text = format.format(sale.valorPagar)

            // Mostrar medio de pago
            val lastFour = sale.numeroTarjeta?.takeLast(4) ?: "XXXX"
            val paymentText = "${sale.tarjeta ?: "Efectivo"} **** $lastFour"
            binding.tvPaymentMethod.text = "Pago: $paymentText"

            // Mostrar estado de la venta
            when (sale.estado) {
                1 -> {
                    binding.tvSaleStatus.text = "Activa"
                    binding.tvSaleStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                }
                0 -> {
                    binding.tvSaleStatus.text = "Cancelada"
                    binding.tvSaleStatus.setBackgroundColor(Color.parseColor("#F44336")) // Rojo
                }
                else -> {
                    binding.tvSaleStatus.text = "Pendiente"
                    binding.tvSaleStatus.setBackgroundColor(Color.parseColor("#FF9800")) // Naranja
                }
            }

            // Lógica de click para FacturacionFragment
            if (onItemClicked != null) {
                binding.root.setOnClickListener {
                    onItemClicked.invoke(sale) // CAMBIO AQUÍ: Pasa el objeto completo
                }
            } else {
                // Desactivar clics si no hay listener (vista de cliente)
                binding.root.isClickable = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(currentSalesList[position])
    }

    override fun getItemCount(): Int = currentSalesList.size

    /**
     * Función para actualizar la lista de ventas (usada por FacturacionFragment)
     */
    fun updateSales(newSales: List<SaleHeader>) {
        currentSalesList = newSales
        notifyDataSetChanged()
    }
}