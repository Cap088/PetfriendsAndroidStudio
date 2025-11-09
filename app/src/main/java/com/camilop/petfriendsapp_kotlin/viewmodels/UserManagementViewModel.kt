package com.camilop.petfriendsapp_kotlin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camilop.petfriendsapp_kotlin.models.UsuarioAdmin
import com.camilop.petfriendsapp_kotlin.models.UsuarioAdminListResponse
import com.camilop.petfriendsapp_kotlin.network.ApiService
import com.camilop.petfriendsapp_kotlin.utils.APIConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserManagementViewModel : ViewModel() {

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(APIConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val _users = MutableLiveData<List<UsuarioAdmin>>()
    val users: LiveData<List<UsuarioAdmin>> get() = _users

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadUsers() {
        _loading.value = true
        _error.value = ""

        viewModelScope.launch {
            try {
                apiService.getAllUsuarios().enqueue(object : Callback<UsuarioAdminListResponse> {
                    override fun onResponse(
                        call: Call<UsuarioAdminListResponse>,
                        response: Response<UsuarioAdminListResponse>
                    ) {
                        if (response.isSuccessful) {
                            _users.value = response.body()?.resultado ?: emptyList()
                        } else {
                            _error.value = "Error: ${response.code()}"
                        }
                        _loading.value = false
                    }

                    override fun onFailure(call: Call<UsuarioAdminListResponse>, t: Throwable) {
                        _error.value = "Fallo: ${t.message}"
                        _loading.value = false
                    }
                })
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _loading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    loadUsers()
                    return@launch
                }

                apiService.searchUsuarios(query).enqueue(object : Callback<UsuarioAdminListResponse> {
                    override fun onResponse(
                        call: Call<UsuarioAdminListResponse>,
                        response: Response<UsuarioAdminListResponse>
                    ) {
                        if (response.isSuccessful) {
                            _users.value = response.body()?.resultado ?: emptyList()
                        } else {
                            _error.value = "Error: ${response.code()}"
                        }
                        _loading.value = false
                    }

                    override fun onFailure(call: Call<UsuarioAdminListResponse>, t: Throwable) {
                        _error.value = "Fallo: ${t.message}"
                        _loading.value = false
                    }
                })
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _loading.value = false
            }
        }
    }
}