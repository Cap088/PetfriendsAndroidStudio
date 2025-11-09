package com.camilop.petfriendsapp_kotlin.network

import com.camilop.petfriendsapp_kotlin.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ==================================================
    // AUTENTICACIÓN
    // ==================================================
    @POST("login")
    fun login(@Body request: LoginRequest): Call<APIResponse>

    @POST("registro")
    fun registro(@Body request: RegisterRequest): Call<APIResponse>

    // ==================================================
    // PRODUCTOS
    // ==================================================
    @GET("productos")
    fun getProducts(): Call<ProductListResponse>

    // ==================================================
    // VENTAS
    // ==================================================
    @GET("ventas/cliente/{idCliente}")
    fun getVentasPorCliente(@Path("idCliente") clientId: Int): Call<VentasHeaderResponse>

    @GET("ventas/todas")
    fun getAllVentas(): Call<VentasAdminResponse>

    @GET("ventas/detalle/{idVenta}")
    fun getVentaDetail(@Path("idVenta") idVenta: Int): Call<SaleDetailResponse>

    // ==================================================
    // CLIENTES
    // ==================================================
    @GET("cliente/por-usuario/{usuario}")
    fun getClientePorUsuario(@Path("usuario") usuario: String): Call<ClienteResponse>

    @GET("clientes/{idCliente}")
    fun getClienteById(@Path("idCliente") idCliente: Int): Call<ClienteResponse>

    @GET("clientes")
    fun getClientes(): Call<ClienteListResponse>

    @GET("clientes/buscar")  // ← AGREGAR ESTE MÉTODO
    fun searchClientes(@Query("q") query: String): Call<ClienteListResponse>

    @POST("clientes")
    fun crearCliente(@Body request: ClienteCreateRequest): Call<BaseResponse>

    @PUT("clientes/{id}")
    fun actualizarCliente(
        @Path("id") id: Int,
        @Body cliente: ClienteUpdateRequest
    ): Call<BaseResponse>

    @DELETE("clientes/{id}")
    fun eliminarCliente(@Path("id") id: Int): Call<BaseResponse>

    // ==================================================
    // USUARIOS (ADMIN)
    // ==================================================
    @GET("usuarios/todos")
    fun getAllUsuarios(): Call<UsuarioAdminListResponse>

    @GET("usuarios/buscar")
    fun searchUsuarios(@Query("q") query: String): Call<UsuarioAdminListResponse>

    @POST("usuarios")
    fun crearUsuario(@Body request: UsuarioCreateRequest): Call<BaseResponse>

    @PUT("usuarios/{id}")
    fun actualizarUsuario(
        @Path("id") id: Int,
        @Body usuario: UsuarioUpdateRequest
    ): Call<BaseResponse>

    @PUT("usuarios/estado/{idUsuario}")
    fun updateUsuarioEstado(
        @Path("idUsuario") idUsuario: Int,
        @Body request: Map<String, Int>
    ): Call<BaseResponse>

    @DELETE("usuarios/{id}")
    fun eliminarUsuario(@Path("id") id: Int): Call<BaseResponse>

    // ==================================================
    // DASHBOARD ADMIN
    // ==================================================
    @GET("dashboard/stats")
    fun getDashboardStats(): Call<DashboardStatsResponse>
}