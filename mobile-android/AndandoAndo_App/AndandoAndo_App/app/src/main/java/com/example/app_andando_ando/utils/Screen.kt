package com.example.app_andando_ando.utils

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Mapa")
    object Routes : Screen("routes", "Rutas")
    object Saved : Screen("saved", "Guardados")
    object Profile : Screen("profile", "Perfil")
}