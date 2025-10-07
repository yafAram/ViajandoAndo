package com.example.app_andando_ando.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.example.app_andando_ando.domain.model.LoginRequest
import com.example.app_andando_ando.domain.usecase.LoginUseCase
import com.example.app_andando_ando.utils.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String = "",
    val showGenericError: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val secureStorage: SecureStorage,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, error = "", showGenericError = false) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, error = "", showGenericError = false) }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = "", showGenericError = false)
            try {
                val resp = loginUseCase(LoginRequest(_uiState.value.email.trim(), _uiState.value.password))
                if (resp.isSuccess && resp.result != null) {
                    // guarda seguro
                    secureStorage.saveToken(resp.result.token)
                    secureStorage.saveUser(gson.toJson(resp.result.user))
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(error = resp.message.ifBlank { "Credenciales inválidas" })
                }
            } catch (e: Exception) {
                // no mostrar traza, mostrar diálogo genérico
                _uiState.value = _uiState.value.copy(showGenericError = true)
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}

