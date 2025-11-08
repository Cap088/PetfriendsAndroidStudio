package com.camilop.petfriendsapp_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.camilop.petfriendsapp_kotlin.databinding.FragmentContactBinding
import es.dmoral.toasty.Toasty

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toasty.error(requireContext(), "Por favor, completa todos los campos.", Toasty.LENGTH_SHORT).show()
            return
        }

        // Simulación de envío de correo
        Toasty.success(
            requireContext(),
            "Mensaje enviado con éxito. Te responderemos pronto.",
            Toasty.LENGTH_LONG
        ).show()

        // Limpiar formulario
        binding.etName.setText("")
        binding.etEmail.setText("")
        binding.etMessage.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
