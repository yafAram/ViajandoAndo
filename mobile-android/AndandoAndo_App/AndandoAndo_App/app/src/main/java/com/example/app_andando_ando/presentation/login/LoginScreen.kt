package com.example.app_andando_ando.presentation.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_andando_ando.R
import com.example.app_andando_ando.ui.components.WhiteActionButton

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var showPassword by remember { mutableStateOf(false) }
    var emailValidationError by remember { mutableStateOf<String?>(null) }
    var showGenericErrorDialog by remember { mutableStateOf(false) }
    var showInlineCredentialError by remember { mutableStateOf(false) }

    // Keep a local dialog flag controlled from state (ViewModel may not expose a dismiss)
    LaunchedEffect(state.error, state.showGenericError) {
        showInlineCredentialError = state.error.isNotBlank() && !state.showGenericError
        showGenericErrorDialog = state.showGenericError
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.bg_statue),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xCC000000), Color(0x88000000))))
        )

        // Content aligned near bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Inicia sesión!",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.88f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {
                        viewModel.onEmailChange(it)
                        if (emailValidationError != null) emailValidationError = null
                    },
                    label = { Text("Email:", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    placeholder = { Text("Email", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )

                // Password
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Contraseña:", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    placeholder = { Text("Contraseña", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        androidx.compose.material3.IconButton(onClick = { showPassword = !showPassword }) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = if (showPassword) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off),
                                contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
                )

                // Inline credential error
                if (showInlineCredentialError) {
                    Text(
                        text = state.error.ifBlank { "Email/Contraseña son incorrectas" },
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                if (!emailValidationError.isNullOrEmpty()) {
                    Text(
                        text = emailValidationError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Text(
                    text = "¿Aún no tienes una cuenta? Crea una aquí",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable {
                            focusManager.clearFocus()
                            onNavigateToRegister()
                        }
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // White button controlled component (guarantees text visible)
                WhiteActionButton(
                    text = "Iniciar Sesión",
                    loading = state.loading,
                    enabled = true,
                    onClick = {
                        focusManager.clearFocus()
                        val email = state.email.trim()
                        val gmailPattern = Regex("^[A-Za-z0-9+_.-]+@gmail\\.com\$")
                        if (!gmailPattern.matches(email)) {
                            emailValidationError = "Por favor ingresa un correo @gmail.com válido"
                            return@WhiteActionButton
                        } else {
                            emailValidationError = null
                        }
                        viewModel.login(onSuccess = onLoginSuccess)
                    }
                )
            }

            Spacer(modifier = Modifier.height(42.dp))
        }
    }

    // Generic error dialog (no stacktrace)
    if (showGenericErrorDialog) {
        AlertDialog(
            onDismissRequest = { showGenericErrorDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showGenericErrorDialog = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text(text = "Error") },
            text = { Text("Error, inténtelo más tarde") }
        )
    }
}
