package com.example.app_andando_ando.presentation.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.domain.model.RegisterRequest
import com.example.app_andando_ando.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String = "",

    val nameError: String = "",
    val emailError: String = "",
    val phoneError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",

    val showGenericError: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onEmail(v: String) { _uiState.value = _uiState.value.copy(email = v, emailError = "", error = "") }
    fun onName(v: String) { _uiState.value = _uiState.value.copy(name = v, nameError = "", error = "") }
    fun onPhone(v: String) { _uiState.value = _uiState.value.copy(phone = v, phoneError = "", error = "") }
    fun onPassword(v: String) { _uiState.value = _uiState.value.copy(password = v, passwordError = "", error = "") }
    fun onConfirmPassword(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, confirmPasswordError = "", error = "") }

    fun dismissGenericError() {
        _uiState.value = _uiState.value.copy(showGenericError = false, error = "")
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                nameError = "",
                emailError = "",
                phoneError = "",
                passwordError = "",
                confirmPasswordError = "",
                error = ""
            )

            val s = _uiState.value
            var hasError = false

            if (s.name.trim().isEmpty()) {
                _uiState.value = _uiState.value.copy(nameError = "Ingresa tu nombre")
                hasError = true
            }

            val emailTrim = s.email.trim()
            if (emailTrim.isEmpty()) {
                _uiState.value = _uiState.value.copy(emailError = "Ingresa un email")
                hasError = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()) {
                _uiState.value = _uiState.value.copy(emailError = "Ingresa un email válido")
                hasError = true
            }

            val phoneTrim = s.phone.trim()
            if (phoneTrim.isEmpty()) {
                _uiState.value = _uiState.value.copy(phoneError = "Ingresa un teléfono")
                hasError = true
            } else if (!phoneTrim.all { it.isDigit() } || phoneTrim.length != 10) {
                _uiState.value = _uiState.value.copy(phoneError = "Teléfono debe tener 10 dígitos")
                hasError = true
            }

            val pw = s.password
            if (pw.length < 8) {
                _uiState.value = _uiState.value.copy(passwordError = "La contraseña debe tener al menos 8 caracteres")
                hasError = true
            } else {
                val hasUpper = pw.any { it.isUpperCase() }
                val hasDigit = pw.any { it.isDigit() }
                val hasSymbol = pw.any { !it.isLetterOrDigit() }
                if (!hasUpper || !hasDigit || !hasSymbol) {
                    _uiState.value = _uiState.value.copy(passwordError = "Usa mayúscula, número y símbolo")
                    hasError = true
                }
            }

            if (s.confirmPassword != s.password) {
                _uiState.value = _uiState.value.copy(confirmPasswordError = "Las contraseñas no coinciden")
                hasError = true
            }

            if (hasError) return@launch

            _uiState.value = _uiState.value.copy(loading = true)
            try {
                val req = RegisterRequest(
                    email = s.email.trim(),
                    name = s.name.trim(),
                    phoneNumber = s.phone.trim(),
                    password = s.password,
                    role = "USER"
                )
                val resp = registerUseCase(req)

                if (resp.isSuccess) {
                    _uiState.value = RegisterUiState()
                    onSuccess() // la UI debe navegar al Login (p. ej. popBackStack)
                } else {
                    _uiState.value = _uiState.value.copy(error = resp.message.ifBlank { "Error al registrar" })
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showGenericError = true)
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}
