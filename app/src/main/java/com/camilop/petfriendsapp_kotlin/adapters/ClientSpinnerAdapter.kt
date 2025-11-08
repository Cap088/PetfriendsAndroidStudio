package com.camilop.petfriendsapp_kotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.camilop.petfriendsapp_kotlin.models.FacturableClient


// Adaptador para mostrar la lista de clientes/usuarios
class ClientSpinnerAdapter(
    context: Context,
    private val clients: List<FacturableClient>
) : ArrayAdapter<FacturableClient>(context, 0, clients) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val client = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_spinner_dropdown_item, parent, false
        )

        val textView = view.findViewById<TextView>(android.R.id.text1)

        textView.text = "${client?.nombre} (ID: ${client?.id})"
        textView.textSize = 16f
        textView.setPadding(16, 16, 16, 16)
        return view
    }
}
