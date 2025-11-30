package com.camilop.petfriendsapp_kotlin.models

// MODELOS DE AUTENTICACIÓN Y USUARIO

// Modelo para la petición de LOGIN

data class LoginRequest(
    val usuario: String,
    val contrasena: String
)

// Modelo para la petición de REGISTRO

data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val usuario: String,
    val contrasena: String,
    val telefono: String
)

// Modelo para los datos del usuario devuelto en login/registro

data class User(
    val nombre: String,
    val apellido: String,
    val usuario: String,
    val telefono: String,
    val rol: String? = null,
    val idCliente: Int? = null
)

// Respuesta general de la API para operaciones de autenticación

data class APIResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: User?
)

// MODELOS DE PRODUCTOS

// Modelo de producto individual

data class Product(
    val idProducto: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val cantidad: Int,
    val porcentajeIva: Double = 16.0,
    val imagenes: String? = null
)

// Respuesta de la lista de productos

data class ProductListResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: List<Product>
)

// MODELOS DE VENTAS

// Detalle individual de un producto en una venta

data class DetalleVentaRequest(
    val idProducto: Int,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val descuento: Double,
    val iva: Double,
    val totalPagar: Double
)

// Solicitud completa para registrar una nueva venta

data class VentaRequest(
    val idCliente: Int,
    val valorPagar: Double,
    val tarjeta: String,
    val numeroTarjeta: String,
    val productos: List<DetalleVentaRequest>
)

// Resultado de una venta registrada

data class VentaResultado(
    val idVenta: Int
)

// Respuesta al registrar una venta

data class VentaResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: VentaResultado?,
    val error: String? = null
)

// MODELOS DE CONSULTA DE VENTAS

// Cabecera de venta para historial de compras

data class SaleHeader(
    val idVenta: Int,
    val idCliente: Int,
    val valorPagar: Double,
    val fecha: String,
    val estado: Int,
    val tarjeta: String?,
    val numeroTarjeta: String?
)

// Detalle de un ítem dentro de una venta

data class SaleDetail(
    val idDetalleVenta: Int,
    val idCabeceraVenta: Int,
    val idProducto: Int,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val descuento: Double,
    val iva: Double,
    val totalPagar: Double
)

// Respuesta de lista de cabeceras de venta

data class VentasHeaderResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: List<SaleHeader>?
)

// MODELOS DE VENTA COMPLETA (FACTURACIÓN)

// Cabecera de venta para facturación

data class CabeceraVenta(
    val idCabeceraVenta: Int,
    val idCliente: Int,
    val valorPagar: Double,
    val fechaVenta: String,
    val estado: Int,
    val tarjeta: String?,
    val numeroTarjeta: String?,
    val cliente_nombre: String?,
    val cliente_apellido: String?,
    val cliente_cedula: String?,
    val cliente_direccion: String?
)

// Detalle de venta para facturación

data class DetalleVenta(
    val idDetalleVenta: Int,
    val idCabeceraVenta: Int,
    val idProducto: Int,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val descuento: Double,
    val iva: Double,
    val totalPagar: Double
)

// Venta completa con cabecera y detalle

data class VentaCompleta(
    val cabecera: CabeceraVenta,
    val detalle: List<DetalleVenta>
)

// Respuesta del detalle completo de una venta

data class SaleDetailResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: VentaCompleta?
)

// MODELOS DE ADMINISTRACIÓN

// Información de usuario para administración

data class UsuarioInfo(
    val idUsuario: Int,
    val nombre: String,
    val apellido: String,
    val rol: String
)

// Modelo completo de cliente

data class Cliente(
    val idCliente: Int,
    val nombre: String,
    val apellido: String,
    val cedula: String,
    val telefono: String,
    val direccion: String
)

// Cliente facturable (lista simplificada)

data class FacturableClient(
    val id: Int,
    val nombre: String,
    val identificacion: String
)

// Resultado de consulta de cliente

data class ClienteResultado(
    val esAdmin: Boolean,
    val usuarioInfo: UsuarioInfo? = null,
    val cliente: Cliente? = null
)

// Respuesta de consulta de cliente

data class ClienteResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: ClienteResultado?
)

// Venta con información de cliente para administrador

data class VentaAdmin(
    val idVenta: Int,
    val idCliente: Int,
    val valorPagar: Double,
    val fecha: String,
    val estado: Int,
    val tarjeta: String?,
    val numeroTarjeta: String?,
    val cliente_nombre: String,
    val cliente_apellido: String
)


// Respuesta de lista de ventas para administrador

data class VentasAdminResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: List<VentaAdmin>?
)

//Respuesta de lista de usuarios para administrador
data class UsuarioListResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: List<UsuarioInfo>?
)

//Respuesta de lista de clientes para administrador
data class ClienteListResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: List<Cliente>?
)

//Request para actualizar usuario
data class UsuarioUpdateRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val rol: String,
    val estado: Boolean
)

//Request para actualizar cliente
data class ClienteUpdateRequest(
    val nombre: String,
    val apellido: String,
    val cedula: String,
    val telefono: String,
    val direccion: String
)

// Request para crear nuevo usuario

data class UsuarioCreateRequest(
    val nombre: String,
    val apellido: String,
    val usuario: String,
    val contrasena: String,
    val rol: String,
    val correo: String
)


// Request para crear nuevo cliente

data class ClienteCreateRequest(
    val nombre: String,
    val apellido: String,
    val cedula: String,
    val telefono: String,
    val direccion: String,
    val idUsuario: Int
)


// Respuesta general de la API genérica para operaciones CRUD

data class BaseResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: Any? = null
)

// MODELOS PARA ADMIN DASHBOARD
data class DashboardStats(
    val totalUsuarios: Int,
    val totalClientes: Int,
    val totalVentas: Int,
    val totalIngresos: Double
)

data class DashboardStatsResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: DashboardStats?
)

// Modelo extendido de Usuario para administración
data class UsuarioAdmin(
    val idUsuario: Int,
    val nombre: String,
    val apellido: String,
    val usuario: String,
    val telefono: String,
    val rol: String,
    val estado: Int
)

// Respuesta de lista de usuarios para administración
data class UsuarioAdminListResponse(
    val codigo: String,
    val mensaje: String,
    val resultado: List<UsuarioAdmin>?
)

