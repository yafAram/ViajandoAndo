// src/main/java/com/example/app_andando_ando/presentation/mapUser/MapUserScreen.kt
package com.example.app_andando_ando.presentation.mapUser

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.app_andando_ando.presentation.mapUser.MapScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_andando_ando.presentation.mapUser.RouteViewModel
import com.example.app_andando_ando.utils.parseSimplePolyline
import com.example.app_andando_ando.presentation.mapUser.addMarker
import com.example.app_andando_ando.presentation.mapUser.addPolyline
import org.osmdroid.views.MapView
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val TAG = "MapUserScreen"
private const val MAPS_BASE_URL = "https://viajandoandomapuserpoint.runasp.net" // si tu backend devuelve rutas relativas

/**
 * UI-only model para waypoints en la lista. Si tus waypoints reales provienen de otra estructura,
 * adapta la conversión donde se añaden elementos.
 */
data class RouteWaypoint(
    val poiId: String? = null,
    val name: String = "",
    val lat: Double,
    val lng: Double,
    val imageUrl: String? = null
)

@Composable
fun MapUserScreen(
    routeViewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by routeViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Origen (inputs)
    var originLatText by remember { mutableStateOf("19.0420") }
    var originLngText by remember { mutableStateOf("-98.2063") }

    // Waypoint manual add
    var wpLat by remember { mutableStateOf("") }
    var wpLng by remember { mutableStateOf("") }
    var wpName by remember { mutableStateOf("") } // opcional, si quieres nombre al añadir

    // Lista local de waypoints (editable). Se puede poblar desde RouteCache si lo deseas.
    val waypointsState = remember { mutableStateListOf<RouteWaypoint>() }

    // Referencia al MapView
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Drag & drop state
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOverIndex by remember { mutableStateOf<Int?>(null) }

    // Snackbar pegado al navbar (bottom). Ajusta paddingBottom si tu barra ocupa otro alto.
    val snackbarHostState = remember { SnackbarHostState() }

    // Dimensiones (para cálculo simple de swap)
    val itemHeightDp = 72.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.toPx() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Planear ruta", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Origen inputs
            Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Origen", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = originLatText,
                            onValueChange = { originLatText = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = originLngText,
                            onValueChange = { originLngText = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add waypoint row
            Surface(tonalElevation = 1.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Agregar waypoint (manual)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = wpLat,
                            onValueChange = { wpLat = it },
                            label = { Text("Lat") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = wpLng,
                            onValueChange = { wpLng = it },
                            label = { Text("Lng") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = wpName,
                            onValueChange = { wpName = it },
                            label = { Text("Nombre (opcional)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            val lat = wpLat.toDoubleOrNull()
                            val lng = wpLng.toDoubleOrNull()
                            if (lat == null || lng == null) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Lat/Lng inválidos") }
                                return@IconButton
                            }
                            // Añadimos sin imageUrl; si vienes de POI puedes usar RouteWaypoint(poiId=..., imageUrl=...)
                            waypointsState.add(RouteWaypoint(name = wpName.ifBlank { "WP${waypointsState.size + 1}" }, lat = lat, lng = lng))
                            mapViewRef?.let { mv -> addMarker(mv.context, mv, lat, lng, waypointsState.last().name) }
                            wpLat = ""
                            wpLng = ""
                            wpName = ""
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MAP AREA
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
            ) {
                MapScreen(modifier = Modifier.fillMaxSize()) { mv ->
                    mapViewRef = mv
                    // dibujar origen y waypoints actuales
                    originLatText.toDoubleOrNull()?.let { lat ->
                        originLngText.toDoubleOrNull()?.let { lng ->
                            addMarker(mv.context, mv, lat, lng, "Origen")
                        }
                    }
                    waypointsState.forEachIndexed { idx, p ->
                        addMarker(mv.context, mv, p.lat, p.lng, p.name.ifBlank { "WP${idx + 1}" })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RUTA PLANIFICADA - área con fondo oscuro solicitado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3B3B3B), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Ruta planificada", color = Color(0xFFF1F1F1), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (waypointsState.isEmpty()) {
                        Text("Añade waypoints para construir la ruta", color = Color(0xFFAAAAAA))
                    } else {
                        // Lista reorderable mediante long-press + drag (implementación simple)
                        LazyColumn {
                            itemsIndexed(waypointsState) { idx, wp ->
                                val isDragging = draggingIndex == idx
                                val scale by animateFloatAsState(if (isDragging) 1.02f else 1f)
                                // Construir URL de imagen si existe
                                val fullImg = wp.imageUrl?.let {
                                    when {
                                        it.startsWith("http://") || it.startsWith("https://") -> it
                                        it.startsWith("/") -> MAPS_BASE_URL.trimEnd('/') + it
                                        else -> it
                                    }
                                }

                                // item content
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(itemHeightDp)
                                        .padding(vertical = 4.dp)
                                        .background(if (isDragging) Color(0xFF4A4A4A) else Color(0xFF444444), RoundedCornerShape(8.dp))
                                        .pointerInput(idx) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingIndex = idx
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    // calculamos índice objetivo aproximado sumando el desplazamiento acumulado
                                                    // para mantener simple: si arrastramos suficiente se intercambia con el vecino
                                                    val dy = dragAmount.y
                                                    if (dy < -itemHeightPx / 2 && idx > 0) {
                                                        // mover hacia arriba
                                                        val target = idx - 1
                                                        waypointsState.removeAt(idx).also { moved ->
                                                            waypointsState.add(target, moved)
                                                            draggingIndex = target
                                                        }
                                                    } else if (dy > itemHeightPx / 2 && idx < waypointsState.size - 1) {
                                                        // mover hacia abajo
                                                        val target = idx + 1
                                                        waypointsState.removeAt(idx).also { moved ->
                                                            waypointsState.add(target, moved)
                                                            draggingIndex = target
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggingIndex = null
                                                },
                                                onDragCancel = {
                                                    draggingIndex = null
                                                }
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectTapGestures(onLongPress = {
                                                // instrucción para usuario: "mantén presionado y arrastra"
                                            })
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Imagen si existe
                                    Box(modifier = Modifier.size(56.dp).background(Color(0xFF2F2F2F), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                                        if (!fullImg.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(fullImg).crossfade(true).build(),
                                                contentDescription = wp.name,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(wp.name.takeIf { it.isNotBlank() }?.firstOrNull()?.toString() ?: "?", color = Color.White, fontSize = 18.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(wp.name.ifBlank { "Waypoint ${idx + 1}" }, color = Color(0xFFF1F1F1), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${wp.lat}, ${wp.lng}", color = Color(0xFFCCCCCC), style = MaterialTheme.typography.bodySmall)
                                    }

                                    // eliminar (pequeño botón)
                                    IconButton(onClick = {
                                        waypointsState.removeAt(idx)
                                        // redibujar marcadores en el mapa
                                        mapViewRef?.let { mv ->
                                            mv.overlays.clear()
                                            originLatText.toDoubleOrNull()?.let { lat -> originLngText.toDoubleOrNull()?.let { lng -> addMarker(mv.context, mv, lat, lng, "Origen") } }
                                            waypointsState.forEachIndexed { i, p -> addMarker(mv.context, mv, p.lat, p.lng, p.name.ifBlank { "WP${i+1}" }) }
                                            mv.invalidate()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE57474))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resultado (igual que antes)
            uiState.result?.let { res ->
                Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Resultado", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Distancia total: ${res.totalDistanceMeters} m", style = MaterialTheme.typography.bodyMedium)
                        Text("Duración estimada: ${res.totalDurationSeconds} s", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LaunchedEffect(res.polyline, mapViewRef) {
                            val pairs = parseSimplePolyline(res.polyline)
                            mapViewRef?.let { mv ->
                                mv.overlays.clear()
                                originLatText.toDoubleOrNull()?.let { lat -> originLngText.toDoubleOrNull()?.let { lng -> addMarker(mv.context, mv, lat, lng, "Origen") } }
                                waypointsState.forEachIndexed { idx, p -> addMarker(mv.context, mv, p.lat, p.lng, p.name.ifBlank { "WP${idx+1}" }) }
                                addPolyline(mv, pairs)
                                mv.invalidate()
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.error.takeIf { it.isNotEmpty() }?.let { err ->
                Text(text = err, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(80.dp)) // espacio para footer
        }

        // Footer: Optimizar (fijo bottom)
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(12.dp)) {
            Button(
                onClick = {
                    val oLat = originLatText.toDoubleOrNull()
                    val oLng = originLngText.toDoubleOrNull()
                    if (oLat == null || oLng == null) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Origen inválido") }
                        return@Button
                    }
                    if (waypointsState.isEmpty()) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Añade al menos 1 waypoint") }
                        return@Button
                    }
                    // transformamos a lista de pares para el ViewModel (no cambié la lógica del viewmodel)
                    val pairs = waypointsState.map { it.lat to it.lng }
                    routeViewModel.optimize(oLat, oLng, pairs, "driving")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculando...", fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Optimizar ruta", fontSize = 16.sp)
                }
            }
        }

        // Snackbar pegado al navbar (asumo altura navbar ~56dp, por eso padding bottom 64)
        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp))
        }
    }
}
