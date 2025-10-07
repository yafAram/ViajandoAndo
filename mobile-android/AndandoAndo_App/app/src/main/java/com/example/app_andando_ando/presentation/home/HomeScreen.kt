package com.example.app_andando_ando.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_andando_ando.R
import com.example.app_andando_ando.ui.components.WhiteActionButton

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val ctx = LocalContext.current

    var showToken by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Text(text = "Bienvenido a ViajandoAndo!", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = if (state.name.isNotBlank()) "Hola, ${state.name}" else "Hola", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Información de usuario", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Nombre: ${if (state.name.isNotBlank()) state.name else "-"}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Email: ${if (state.email.isNotBlank()) state.email else "-"}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Teléfono: ${if (state.phone.isNotBlank()) state.phone else "-"}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Token", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))

                        IconButton(onClick = { showToken = !showToken }) {
                            Icon(
                                painter = painterResource(id = if (showToken) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on),
                                contentDescription = if (showToken) "Ocultar token" else "Mostrar token"
                            )
                        }

                        IconButton(onClick = {
                            if (state.token.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(state.token))
                                Toast.makeText(ctx, "Token copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "No hay token para copiar", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(painter = painterResource(id = R.drawable.ic_copy), contentDescription = "Copiar token")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (state.token.isNotBlank()) {
                            if (showToken) state.token else state.token.take(40) + if (state.token.length > 40) "… (oculto)" else ""
                        } else {
                            "No token disponible"
                        },
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Logout using WhiteActionButton (consistent look)
            WhiteActionButton(
                text = "Cerrar sesión",
                loading = false,
                enabled = true,
                onClick = { viewModel.logout(onLoggedOut) }
            )
        }
    }
}