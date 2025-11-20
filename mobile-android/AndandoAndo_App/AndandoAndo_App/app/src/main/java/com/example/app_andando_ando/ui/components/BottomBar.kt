package com.example.app_andando_ando.presentation.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.app_andando_ando.utils.Screen
import com.example.app_andando_ando.ui.theme.BottomBarBackground
import androidx.compose.ui.Modifier

@Composable
fun BottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val items: List<Screen> = listOf(
        Screen.Home,
        Screen.Routes,
        Screen.Saved,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = BottomBarBackground, modifier = modifier) {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    val icon = when (screen) {
                        Screen.Home -> Icons.Default.Map
                        Screen.Routes -> Icons.Default.Explore
                        Screen.Saved -> Icons.Default.Bookmark
                        Screen.Profile -> Icons.Default.Person
                    }
                    Icon(imageVector = icon, contentDescription = screen.label)
                },
                label = { Text(screen.label) }
            )
        }
    }
}

