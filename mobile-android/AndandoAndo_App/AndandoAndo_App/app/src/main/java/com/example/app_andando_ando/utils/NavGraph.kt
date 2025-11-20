package com.example.app_andando_ando.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_andando_ando.presentation.components.BottomBar
import com.example.app_andando_ando.presentation.home.HomeScreen
import com.example.app_andando_ando.ui.theme.App_Andando_AndoTheme
import com.example.app_andando_ando.utils.Screen
import com.example.app_andando_ando.presentation.route.RouteScreen
import com.example.app_andando_ando.presentation.user.UserScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    App_Andando_AndoTheme {
        Scaffold(
            bottomBar = { BottomBar(navController = navController) }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = androidx.compose.ui.Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) {
                    // HomeScreen ahora espera navController directamente (no onPoiSelected)
                    HomeScreen(
                        navController = navController,
                        onLoggedOut = {
                            // implementación logout: por ejemplo, navegar a login
                            // navController.navigate("login") { popUpTo(Screen.Home.route) { inclusive = true } }
                        }
                    )
                }

                composable(Screen.Routes.route) {
                    RouteScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Saved.route) {
                    Text("Guardados")
                }

                composable(Screen.Profile.route) {
                    UserScreen(
                        onEditProfile = { /* navegar a editar perfil */ },
                        onLogoutNav = {
                            // ejemplo de navegación tras logout desde UserScreen
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
