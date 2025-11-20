package com.example.app_andando_ando.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ButtonPink,
    onPrimary = ButtonTextDark,
    secondary = ButtonPink,
    background = AppBackground,
    surface = CardSurface,
    onSurface = PoiTextPrimary
)

@Composable
fun App_Andando_AndoTheme(
    content: @Composable () -> Unit
) {
    // Forzamos tema claro para respetar diseño proporcionado
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
