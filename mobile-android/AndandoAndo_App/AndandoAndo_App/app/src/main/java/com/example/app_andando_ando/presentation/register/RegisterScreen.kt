package com.example.app_andando_ando.presentation.register

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
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.register),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xCC000000), Color(0x88000000))))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Regístrate!",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onName(it) },
                    label = { Text("Nombre completo", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default
                )
                if (state.nameError.isNotBlank()) {
                    Text(text = state.nameError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }

                // Email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmail(it) },
                    label = { Text("Email", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
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
                if (state.emailError.isNotBlank()) {
                    Text(text = state.emailError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }

                // Teléfono
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = { viewModel.onPhone(it) },
                    label = { Text("Teléfono", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                )
                if (state.phoneError.isNotBlank()) {
                    Text(text = state.phoneError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }

                // Contraseña
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPassword(it) },
                    label = { Text("Contraseña", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    placeholder = { Text("Mínimo 8 caracteres", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
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
                        .height(60.dp)
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
                if (state.passwordError.isNotBlank()) {
                    Text(text = state.passwordError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }

                // Confirmar contraseña
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onConfirmPassword(it) },
                    label = { Text("Confirmar contraseña", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        androidx.compose.material3.IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = if (showConfirmPassword) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off),
                                contentDescription = if (showConfirmPassword) "Ocultar" else "Mostrar",
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
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
                if (state.confirmPasswordError.isNotBlank()) {
                    Text(text = state.confirmPasswordError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Registrar
                WhiteActionButton(
                    text = "Registrar",
                    loading = state.loading,
                    enabled = !state.loading,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.register(onSuccess = onRegisterSuccess)
                    }
                )

                if (state.error.isNotBlank() && !state.showGenericError) {
                    Text(text = state.error, color = Color(0xFFFF6B6B), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Volver",
                    color = Color.White,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(vertical = 6.dp)
                )
            }
        }
    }

    // Generic error dialog uses viewModel.dismissGenericError() (exists in RegisterViewModel)
    if (state.showGenericError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGenericError() },
            title = { Text("Error") },
            text = { Text("Error, inténtelo más tarde") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.dismissGenericError() }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
