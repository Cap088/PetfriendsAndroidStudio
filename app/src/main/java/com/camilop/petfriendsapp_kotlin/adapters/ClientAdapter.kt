package com.camilop.petfriendsapp_kotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.databinding.ItemClientBinding
import com.camilop.petfriendsapp_kotlin.models.Cliente

class ClientAdapter(
    private val onEditClient: (Cliente) -> Unit,
    private val onViewSales: (Cliente) -> Unit
) : ListAdapter<Cliente, ClientAdapter.ClientViewHolder>(ClientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = getItem(position)
        holder.bind(client)
    }

    inner class ClientViewHolder(private val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(client: Cliente) {
            binding.tvClientName.text = "${client.nombre} ${client.apellido}"
            binding.tvClientCedula.text = "Cédula: ${client.cedula}"
            binding.tvClientPhone.text = "Teléfono: ${client.telefono}"
            binding.tvClientAddress.text = "Dirección: ${client.direccion}"

            // Listeners
            binding.btnEditClient.setOnClickListener {
                onEditClient(client)
            }

            // Click en toda la tarjeta
            binding.root.setOnClickListener {
                onEditClient(client)
            }
        }
    }

    class ClientDiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem.idCliente == newItem.idCliente
        }

        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem == newItem
        }
    }
}