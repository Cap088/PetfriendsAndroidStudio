package com.camilop.petfriendsapp_kotlin.utils

import com.camilop.petfriendsapp_kotlin.models.Product


//Manejar el carrito usando un Map para rastrear cantidades.
//Map: { Producto -> Cantidad }

object CartManager {

    // Cambiamos a un mapa para rastrear el producto y su cantidad
    private val cartItems = mutableMapOf<Product, Int>()

    //Añade una unidad del producto al carrito.
    fun addItem(product: Product) {
        // Incrementa la cantidad si ya existe, si no, la inicializa en 1.
        cartItems[product] = cartItems.getOrDefault(product, 0) + 1
    }


    //Elimina una unidad del producto.
    //Retorna true si se eliminó el ítem por completo.

    fun removeOneItem(product: Product): Boolean {
        val currentQuantity = cartItems.getOrDefault(product, 0)

        if (currentQuantity <= 1) {
            // Si la cantidad es 1 o menos, se elimina el producto del mapa.
            cartItems.remove(product)
            return true
        } else {
            // Si la cantidad es mayor que 1, solo se resta 1.
            cartItems[product] = currentQuantity - 1
            return false
        }
    }

    //Elimina completamente un producto del carrito.
    fun removeProduct(product: Product) {
        cartItems.remove(product)
    }

    //Devuelve una lista de las entradas del carrito (Producto y Cantidad).
    fun getCartItems(): List<Map.Entry<Product, Int>> {
        return cartItems.entries.toList()
    }

    //Calcula el subtotal total de todos los productos en el carrito (Precio * Cantidad).
    fun getCartSubtotal(): Double {
        return cartItems.map { (product, quantity) -> product.precio * quantity }.sum()
    }

    //Vacía completamente el carrito.
    fun clearCart() {
        cartItems.clear()
    }
}