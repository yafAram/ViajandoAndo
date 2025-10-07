package com.example.app_andando_ando.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.utils.SecureStorage
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class AuthUser(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String = ""
)

data class HomeUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val token: String = "",
    val loading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadAuthInfo()
    }

    private fun loadAuthInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)

            val token = try { secureStorage.getToken() } catch (_: Exception) { null }
            val userJson = try { secureStorage.getUserJson() } catch (_: Exception) { null }

            var name = ""
            var email = ""
            var phone = ""

            if (!userJson.isNullOrBlank()) {
                try {
                    val user = gson.fromJson(userJson, AuthUser::class.java)
                    name = user.name
                    email = user.email
                    phone = user.phoneNumber
                } catch (_: Exception) {
                    // ignore parse errors
                }
            }

            _uiState.value = HomeUiState(
                name = name,
                email = email,
                phone = phone,
                token = token ?: "",
                loading = false
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            secureStorage.clearUser()
            secureStorage.clearToken()
            onLoggedOut()
        }
    }
}
