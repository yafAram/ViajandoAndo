// src/main/java/.../presentation/home/HomeScreen.kt
package com.example.app_andando_ando.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.app_andando_ando.presentation.components.CategoryRow
import com.example.app_andando_ando.presentation.components.PoiListItem
import com.example.app_andando_ando.presentation.components.RadioRangeButton
import com.example.app_andando_ando.presentation.components.SearchBar
import com.example.app_andando_ando.ui.theme.AppBackground
import com.example.app_andando_ando.ui.theme.HeaderDarkOval
import com.example.app_andando_ando.utils.Screen
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun HomeScreen(
    poisViewModel: PoisViewModel = hiltViewModel(),
    navController: NavController,
    onLoggedOut: () -> Unit = {}
) {
    val uiState by poisViewModel.uiState.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    var radius by rememberSaveable { mutableStateOf(2000) }
    val radiusOptions = listOf(2000, 5000, 20000, 100000, 190910)

    // animationKey se incrementa cuando:
    //  - volvemos a Home en la pila de navegación
    //  - cambiamos filtros (radius, selectedCategory, query)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var animationKey by remember { mutableStateOf(0) }

    // bump cuando la ruta activa es Home (al volver)
    LaunchedEffect(navBackStackEntry?.destination?.route) {
        if (navBackStackEntry?.destination?.route == Screen.Home.route) {
            animationKey++
        }
    }

    // bump cuando cambian filtros / búsqueda (para reiniciar animación)
    LaunchedEffect(radius, selectedCategory, query) {
        // recarga datos
        val lat = 19.0420
        val lng = -98.2063
        poisViewModel.loadPois(
            lat = lat,
            lng = lng,
            radius = radius,
            category = selectedCategory,
            name = if (query.isBlank()) null else query
        )

        // reiniciamos animación cuando aplicamos filtro/búsqueda
        animationKey++
    }

    // header + list entrance anims
    val headerOffsetY = remember { Animatable(-40f) }
    val headerAlpha = remember { Animatable(0f) }
    val listOffsetY = remember { Animatable(20f) }
    val listAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerOffsetY.animateTo(0f, animationSpec = tween(500))
        headerAlpha.animateTo(1f, animationSpec = tween(450))
        delay(120)
        listOffsetY.animateTo(0f, animationSpec = tween(450))
        listAlpha.animateTo(1f, animationSpec = tween(400))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = HeaderDarkOval,
                        shape = RoundedCornerShape(bottomStart = 120.dp, bottomEnd = 120.dp)
                    )
                    .offset(y = headerOffsetY.value.dp)
                    .alpha(headerAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "¿A dónde quieres ir hoy?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        placeholder = "Buscar lugares"
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // filtros (categorías + radio)
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .offset(y = listOffsetY.value.dp)
                    .alpha(listAlpha.value)
            ) {
                Text(
                    text = "Cerca de ti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                CategoryRow(
                    categories = listOf("Todos", "Comida", "Atracciones", "Alojamiento"),
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                RadioRangeButton(
                    options = radiusOptions,
                    selectedMeters = radius,
                    onSelected = { radius = it }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // loading / error
        item {
            if (uiState.loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = uiState.error, color = androidx.compose.ui.graphics.Color.Black)
                }
            }
        }

        // Lista de POIs con staggered animation por item.
        itemsIndexed(uiState.pois, key = { _, p -> p.poiId }) { index, poi ->
            val distanceMeters = runCatching {
                distanceFromMeters(19.0420, -98.2063, poi.latitude, poi.longitude)
            }.getOrNull() ?: Double.NaN

            val subtitle = buildString {
                if (!poi.category.isNullOrBlank()) append(poi.category)
                if (!distanceMeters.isNaN()) {
                    if (isNotBlank()) append(" · ")
                    append(String.format("%.1f km", distanceMeters / 1000.0))
                }
                val descSnippet = poi.description?.takeIf { it.isNotBlank() }?.let { " — ${it.take(60)}${if (it.length>60) "…" else ""}" } ?: ""
                append(descSnippet)
            }

            // visible depende de animationKey y poiId -> se reinicia correctamente
            val visibleState = remember(animationKey, poi.poiId) { mutableStateOf(false) }
            LaunchedEffect(animationKey, poi.poiId, index) {
                val wait = (index * 80).coerceAtMost(600)
                delay(wait.toLong())
                visibleState.value = true
            }

            AnimatedVisibility(
                visible = visibleState.value,
                enter = fadeIn(animationSpec = tween(360)) +
                        slideInVertically(initialOffsetY = { it / 6 }, animationSpec = tween(360))
            ) {
                PoiListItem(
                    name = poi.name,
                    subtitle = subtitle,
                    imageUrl = poi.imageUrl
                ) {
                    val encoded = java.net.URLEncoder.encode(poi.poiId, "UTF-8")
                    navController.navigate("poi_detail/$encoded")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(56.dp)) }
    }
}

/** Haversine - devuelve distancia en metros */
private fun distanceFromMeters(lat1Degrees: Double, lon1Degrees: Double, lat2Degrees: Double, lon2Degrees: Double): Double {
    return try {
        val lat1 = Math.toRadians(lat1Degrees)
        val lon1 = Math.toRadians(lon1Degrees)
        val lat2 = Math.toRadians(lat2Degrees)
        val lon2 = Math.toRadians(lon2Degrees)
        val dlat = lat2 - lat1
        val dlon = lon2 - lon1
        val a = sin(dlat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val R = 6371000.0
        R * c
    } catch (t: Throwable) {
        Double.NaN
    }
}
