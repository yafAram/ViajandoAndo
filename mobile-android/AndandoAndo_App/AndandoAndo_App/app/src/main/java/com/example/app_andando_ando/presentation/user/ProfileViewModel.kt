package com.example.app_andando_ando.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.auth.SessionManager
import com.example.app_andando_ando.domain.model.User
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserUiState(
    val loading: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val error: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState(loading = true))
    val uiState: StateFlow<UserUiState> = _uiState

    private val _logoutCompleted = MutableStateFlow(false)
    val logoutCompleted: StateFlow<Boolean> = _logoutCompleted

    init {
        loadFromSession()
    }

    private fun loadFromSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = "")
            try {
                val userJson = sessionManager.getUserJson()
                val token = sessionManager.getToken()

                val user: User? = if (!userJson.isNullOrBlank()) {
                    // parse safely a Map para evitar problemas con Kotlin non-null fields
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val map = gson.fromJson(userJson, Map::class.java) as? Map<*, *>
                        if (map != null) {
                            val id = map["id"]?.toString() ?: ""
                            val email = map["email"]?.toString() ?: ""
                            val name = map["name"]?.toString() ?: ""
                            val phone = map["phoneNumber"]?.toString() ?: ""
                            User(id = id, email = email, name = name, phoneNumber = phone)
                        } else null
                    } catch (t: Throwable) {
                        null
                    }
                } else null

                _uiState.value = UserUiState(
                    loading = false,
                    user = user,
                    token = token,
                    error = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.localizedMessage ?: "Error cargando perfil")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                sessionManager.clearSession()
            } catch (_: Exception) {
            } finally {
                _logoutCompleted.value = true
            }
        }
    }

    // Preferencias: radio
    fun saveRadiusMeters(radius: Int) {
        viewModelScope.launch {
            try {
                sessionManager.saveRadiusMeters(radius)
            } catch (_: Exception) {}
        }
    }

    fun getRadiusMeters(): Int = sessionManager.getRadiusMeters()
}
