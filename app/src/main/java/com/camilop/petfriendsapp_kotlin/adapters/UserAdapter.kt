package com.camilop.petfriendsapp_kotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.camilop.petfriendsapp_kotlin.R
import com.camilop.petfriendsapp_kotlin.databinding.ItemUserBinding
import com.camilop.petfriendsapp_kotlin.models.UsuarioAdmin

class UserAdapter(
    private val onEditUser: (UsuarioAdmin) -> Unit
) : ListAdapter<UsuarioAdmin, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UsuarioAdmin) {
            binding.tvUserName.text = "${user.nombre} ${user.apellido}"
            binding.tvUserUsername.text = user.usuario
            binding.tvUserRole.text = user.rol

            // Configurar color según el rol
            val roleColor = when (user.rol.lowercase()) {
                "admin" -> R.color.yellow
                "cliente" -> R.color.verde
                else -> R.color.yellow
            }
            binding.tvUserRole.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, roleColor)
            )

            // Mostrar estado
            binding.tvUserStatus.text = if (user.estado == 1) "Activo" else "Inactivo"
            binding.tvUserStatus.setTextColor(
                ContextCompat.getColor(binding.root.context,
                    if (user.estado == 1) R.color.verde else R.color.red_status)
            )

            // Listeners
            binding.btnEditUser.setOnClickListener {
                onEditUser(user)
            }

            // Click en toda la tarjeta
            binding.root.setOnClickListener {
                onEditUser(user)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UsuarioAdmin>() {
        override fun areItemsTheSame(oldItem: UsuarioAdmin, newItem: UsuarioAdmin): Boolean {
            return oldItem.idUsuario == newItem.idUsuario
        }

        override fun areContentsTheSame(oldItem: UsuarioAdmin, newItem: UsuarioAdmin): Boolean {
            return oldItem == newItem
        }
    }
}