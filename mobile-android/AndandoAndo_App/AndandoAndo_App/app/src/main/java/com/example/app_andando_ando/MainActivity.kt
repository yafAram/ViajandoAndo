package com.example.app_andando_ando

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app_andando_ando.presentation.components.BottomBar
import com.example.app_andando_ando.presentation.home.HomeScreen
import com.example.app_andando_ando.presentation.poi.PoiDetailScreen
import com.example.app_andando_ando.ui.theme.App_Andando_AndoTheme
import dagger.hilt.android.EntryPointAccessors
import com.example.app_andando_ando.auth.SessionManager
import com.example.app_andando_ando.di.MainActivityEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager
import com.example.app_andando_ando.utils.Screen
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.Composable
import com.example.app_andando_ando.presentation.route.RouteScreen
import com.example.app_andando_ando.presentation.user.UserScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_andando_ando.presentation.home.PoisViewModel
import com.example.app_andando_ando.data.repository.mapUser.PoiRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permisos runtime: revisar si quieres procesarlos */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Obtén SessionManager vía EntryPoint (EntryPointAccessors)
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, MainActivityEntryPoint::class.java)
        val sessionManager: SessionManager = entryPoint.sessionManager()

        // OSMdroid config
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName

        // Pedir permisos runtime si aplica
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (perms.isNotEmpty()) permissionLauncher.launch(perms.toTypedArray())

        val isLoggedIn = sessionManager.getToken() != null

        setContent {
            App_Andando_AndoTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val bottomRoutes = listOf(
                            Screen.Home.route,
                            Screen.Routes.route,
                            Screen.Saved.route,
                            Screen.Profile.route
                        )

                        if (currentRoute != null && bottomRoutes.contains(currentRoute)) {
                            BottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) Screen.Home.route else "login",
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        // HOME
                        composable(Screen.Home.route) {
                            HomeScreen(
                                poisViewModel = hiltViewModel(),
                                navController = navController,
                                onLoggedOut = {
                                    lifecycleScope.launch {
                                        sessionManager.clearSession()
                                        navController.navigate("login") {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // POI DETAIL
                        composable("poi_detail/{poiId}") { backStackEntry ->
                            val raw = backStackEntry.arguments?.getString("poiId") ?: ""
                            val poiId = Uri.decode(raw)
                            PoiDetailScreen(poiId = poiId, navController = navController)
                        }

                        // Placeholders para otras pestañas de la bottom bar
                        composable(Screen.Routes.route) {
                            RouteScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.Saved.route) { PlaceholderScreen("Guardados") }
                        composable(Screen.Profile.route) {
                            UserScreen(
                                onEditProfile = {},
                                onLogoutNav = {
                                    lifecycleScope.launch {
                                        try {
                                            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, MainActivityEntryPoint::class.java)
                                            val sessionManager = entryPoint.sessionManager()
                                            sessionManager.clearSession()
                                        } catch (_: Exception) {}
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Text(text = title)
}


