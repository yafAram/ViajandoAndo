package com.example.app_andando_ando.presentation.route

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.ContextCompat
import com.example.app_andando_ando.utils.metersToKmString
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


private const val TAG = "RouteScreen"
private const val MAPS_BASE_URL = "https://viajandoandomapuserpoint.runasp.net"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onBack: () -> Unit = {},
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val optimizeResponse by viewModel.optimizeResponse.collectAsState()
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // fused location
    val fusedClient = remember(ctx) { LocationServices.getFusedLocationProviderClient(ctx) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }

    var routingActive by remember { mutableStateOf(false) }
    var routePaused by remember { mutableStateOf(false) }

    // followRoute se activa SOLO cuando el usuario presiona "Iniciar Ruta"
    var followRoute by remember { mutableStateOf(false) }

    // Forzar recarga inicial cache
    LaunchedEffect(Unit) {
        viewModel.refreshFromCache()
        Log.d(TAG, "Pantalla iniciada - Actualizando cache")
    }

    // permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms["android.permission.ACCESS_FINE_LOCATION"] == true
        val coarse = perms["android.permission.ACCESS_COARSE_LOCATION"] == true
        if (fine || coarse) {
            scope.launch { currentLocation = getLastKnownLocationAsGeoPoint(ctx, fusedClient) }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de ubicación denegado.") }
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            ctx,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            ctx,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            currentLocation = getLastKnownLocationAsGeoPoint(ctx, fusedClient)
        }
    }

    // actualizaciones de ubicación (solo actualiza currentLocation)
    DisposableEffect(Unit) {
        val request = LocationRequest.create().apply {
            interval = 3000L
            fastestInterval = 1500L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location? = result.lastLocation
                if (loc != null) {
                    currentLocation = GeoPoint(loc.latitude, loc.longitude)
                }
            }
        }
        val permissionOk = androidx.core.content.ContextCompat.checkSelfPermission(
            ctx,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    ctx,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (permissionOk) {
            try {
                fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } catch (_: Exception) { /* ignore */ }
        }
        onDispose {
            try { fusedClient.removeLocationUpdates(callback) } catch (_: Exception) { }
        }
    }

    // sheet controls
    val sheetFraction = remember { Animatable(0f) }
    val collapsedHeightDp = 160.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeightDp.toPx() }
    fun lerpFloat(a: Float, b: Float, t: Float): Float = a + (b - a) * t
    fun toggleSheet(expand: Boolean) { scope.launch { if (expand) sheetFraction.animateTo(1f, tween(300)) else sheetFraction.animateTo(0f, tween(300)) } }

    // estado recordado para evitar redraw constantes
    val rememberedMapState = remember {
        object {
            var lastHash: String = ""
            var lastCenter: GeoPoint? = null
            var lastZoom: Double? = null
            var fitApplied: Boolean = false
            var userInteractedAt: Long = 0L
            var lastMapUpdateMs: Long = 0L
            var initialBuilt: Boolean = false
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Plan de Ruta", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = androidx.compose.ui.graphics.Color.White) }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color(0xFF3B3B3B))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(bottom = 56.dp)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // MAPVIEW
            AndroidView(
                factory = { ctxMap ->
                    MapView(ctxMap).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        setTileSource(TileSourceFactory.MAPNIK)
                        controller.setZoom(12.0)
                        setMultiTouchControls(true)
                        onResume()
                        Configuration.getInstance().load(ctxMap, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctxMap))
                        Configuration.getInstance().userAgentValue = ctxMap.packageName

                        // escucha interacción para desactivar auto-fit temporalmente
                        setOnTouchListener { _: View, event: MotionEvent ->
                            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                                rememberedMapState.userInteractedAt = System.currentTimeMillis()
                            }
                            false
                        }
                    }
                },
                update = { mapView ->
                    val MIN_UPDATE_MS = 20_000L

                    val waypoints = uiState.waypoints
                    val optimize = optimizeResponse
                    val centerLatNullable = currentLocation?.latitude
                    val centerLonNullable = currentLocation?.longitude

                    // build hash (string) of relevant data
                    val ptsForHashBuilder = StringBuilder()
                    ptsForHashBuilder.append(centerLatNullable ?: "null")
                        .append(",")
                        .append(centerLonNullable ?: "null")
                        .append("|")
                    ptsForHashBuilder.append(waypoints.joinToString(";") { wp -> "${wp.lat},${wp.lng},${wp.poiId}" })
                        .append("|")
                        .append(optimize?.polyline ?: "")
                    val ptsForHash = ptsForHashBuilder.toString()

                    val now = System.currentTimeMillis()
                    val userRecentlyInteracted = (now - rememberedMapState.userInteractedAt) < 4_000L
                    val hashChanged = ptsForHash != rememberedMapState.lastHash
                    val enoughTimePassed = (now - rememberedMapState.lastMapUpdateMs) >= MIN_UPDATE_MS

                    val shouldBuildInitial = !rememberedMapState.initialBuilt
                    val shouldRebuildWhileRouting = routingActive && (!userRecentlyInteracted && (hashChanged || enoughTimePassed))

                    if (shouldBuildInitial || shouldRebuildWhileRouting) {
                        rememberedMapState.lastMapUpdateMs = now

                        // Limpia y redibuja overlays
                        mapView.overlays.clear()

                        // marcador user
                        val curLoc = currentLocation
                        if (curLoc != null) {
                            val userMarker = Marker(mapView)
                            userMarker.position = curLoc
                            userMarker.title = "Tu ubicación"
                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            runCatching {
                                ContextCompat.getDrawable(mapView.context, android.R.drawable.presence_online)?.let { d -> userMarker.icon = d }
                            }
                            mapView.overlays.add(userMarker)
                        }

                        // waypoints (numerados)
                        waypoints.forEachIndexed { idx, wp ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(wp.lat, wp.lng)
                            val display = if (wp.name.isNotBlank()) wp.name else wp.poiId
                            marker.title = "${idx + 1}. $display"
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            runCatching {
                                ContextCompat.getDrawable(mapView.context, android.R.drawable.ic_menu_mylocation)?.let { d -> marker.icon = d }
                            }
                            mapView.overlays.add(marker)
                        }

                        // polyline (opt) - azul
                        if (optimize != null && optimize.polyline.isNotBlank()) {
                            try {
                                val rawPts = decodePolylineToGeoPoints(optimize.polyline)
                                val ptsFixed = ensurePolylineStartsAtUser(rawPts, currentLocation, thresholdMeters = 200.0)
                                if (ptsFixed.isNotEmpty()) {
                                    try {
                                        val routeLine = Polyline()
                                        routeLine.setPoints(ptsFixed)
                                        routeLine.isGeodesic = true
                                        routeLine.outlinePaint.strokeWidth = 10f
                                        routeLine.outlinePaint.color = Color.BLUE
                                        // opcional: estilo de capa (si quieres un borde)
                                        mapView.overlays.add(routeLine)
                                        Log.d(TAG, "Route polyline puntos=${ptsFixed.size}, first=${ptsFixed.first().latitude},${ptsFixed.first().longitude}")
                                    } catch (t: Throwable) {
                                        Log.e(TAG, "poly decode/draw error", t)
                                    }
                                }
                            } catch (t: Throwable) { Log.e(TAG, "poly decode error", t) }
                        }

                        // ajustar bounding box si aplica (manual, sin minOf/maxOf)
                        var minLat = Double.POSITIVE_INFINITY
                        var minLon = Double.POSITIVE_INFINITY
                        var maxLat = Double.NEGATIVE_INFINITY
                        var maxLon = Double.NEGATIVE_INFINITY

                        if (curLoc != null) {
                            if (curLoc.latitude < minLat) minLat = curLoc.latitude
                            if (curLoc.latitude > maxLat) maxLat = curLoc.latitude
                            if (curLoc.longitude < minLon) minLon = curLoc.longitude
                            if (curLoc.longitude > maxLon) maxLon = curLoc.longitude
                        }

                        for (wp in waypoints) {
                            if (wp.lat < minLat) minLat = wp.lat
                            if (wp.lat > maxLat) maxLat = wp.lat
                            if (wp.lng < minLon) minLon = wp.lng
                            if (wp.lng > maxLon) maxLon = wp.lng
                        }

                        if (minLat.isFinite() && minLon.isFinite() && maxLat.isFinite() && maxLon.isFinite()) {
                            val latPad = (maxLat - minLat) * 0.04.coerceAtLeast(0.001)
                            val lonPad = (maxLon - minLon) * 0.04.coerceAtLeast(0.001)
                            val bbox = BoundingBox(maxLat + latPad, maxLon + lonPad, minLat - latPad, minLon - lonPad)
                            try {
                                mapView.zoomToBoundingBox(bbox, true)
                                rememberedMapState.lastCenter = GeoPoint((minLat + maxLat) / 2.0, (minLon + maxLon) / 2.0)
                                rememberedMapState.lastZoom = mapView.zoomLevelDouble
                                rememberedMapState.fitApplied = true
                            } catch (_: Throwable) { /* ignore */ }
                        }

                        rememberedMapState.lastHash = ptsForHash
                        rememberedMapState.initialBuilt = true
                    } else {
                        // No rebuild: actualizamos marcador de usuario de forma limitada (solo cuando routingActive y pasó MIN_UPDATE_MS)
                        if (routingActive && enoughTimePassed) {
                            rememberedMapState.lastMapUpdateMs = now
                            val existingUser = mapView.overlays.filterIsInstance<Marker>().firstOrNull { m -> m.title == "Tu ubicación" }
                            val curLoc2 = currentLocation
                            if (curLoc2 != null) {
                                if (existingUser != null) {
                                    existingUser.position = curLoc2
                                } else {
                                    val userMarker = Marker(mapView)
                                    userMarker.position = curLoc2
                                    userMarker.title = "Tu ubicación"
                                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    runCatching {
                                        ContextCompat.getDrawable(mapView.context, android.R.drawable.presence_online)?.let { d -> userMarker.icon = d }
                                    }
                                    mapView.overlays.add(userMarker)
                                }
                            }
                        }
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // panel inferior (UI)
            val expandedHeightDp = (LocalConfiguration.current.screenHeightDp.dp * 0.85f)
            val panelHeightDp by remember {
                derivedStateOf {
                    val frac = sheetFraction.value
                    val collapsedPx = collapsedHeightPx
                    val expandedPx = with(density) { expandedHeightDp.toPx() }
                    val hPx = lerpFloat(collapsedPx, expandedPx, frac)
                    with(density) { hPx.toDp() }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelHeightDp)
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        // tipo explícito de parámetros para evitar inferencia ambigua
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _: Offset -> /* optional cue */ },
                            onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                change.consume()
                                val deltaFraction = -dragAmount.y / screenHeightPx
                                val target = (sheetFraction.value + deltaFraction).coerceIn(0f, 1f)
                                scope.launch { sheetFraction.snapTo(target) }
                            },
                            onDragEnd = {},
                            onDragCancel = {}
                        )
                    }
                    .background(androidx.compose.ui.graphics.Color(0xFF3B3B3B), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val headerTitle = if (routingActive) "Ruta en curso" else "Buscar"

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = headerTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color.White)
                        Text("Mexico City", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.White)
                    }

                    val shouldExpand = sheetFraction.value < 0.5f
                    IconButton(onClick = { toggleSheet(shouldExpand) }) {
                        if (shouldExpand) Icon(Icons.Default.ExpandLess, contentDescription = "Expandir") else Icon(Icons.Default.ExpandMore, contentDescription = "Colapsar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val optimize = optimizeResponse

                if (routingActive) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (optimize != null) {
                            Text("Resumen de la ruta", fontWeight = FontWeight.SemiBold, color = androidx.compose.ui.graphics.Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Distancia: ${metersToKmString(optimize.totalDistanceMeters.toInt())}", color = androidx.compose.ui.graphics.Color.White)
                            Text("Duración: ${secondsToReadable(optimize.totalDurationSeconds.toLong())}", color = androidx.compose.ui.graphics.Color.White)
                        } else {
                            Text("Ruta en curso: Esperando respuesta del servidor...", color = androidx.compose.ui.graphics.Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val pauseLabel = if (routePaused) "Reanudar" else "Pausar"
                            OutlinedButton(onClick = { routePaused = !routePaused }, modifier = Modifier.weight(1f)) { Text(pauseLabel) }
                            Button(onClick = { routingActive = false; routePaused = false }, modifier = Modifier.weight(2f)) { Text("Finalizar viaje") }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        val waypoints = uiState.waypoints
                        if (waypoints.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay puntos en la ruta", color = androidx.compose.ui.graphics.Color.White)
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color(0xFF3B3B3B), shape = RoundedCornerShape(12.dp)).padding(8.dp)) {
                                LazyColumn {
                                    itemsIndexed(waypoints) { index, w ->
                                        val displayName = if (w.name.isNotBlank()) w.name else w.poiId
                                        val fullImg = w.imageUrl?.let { img ->
                                            when {
                                                img.startsWith("http://") || img.startsWith("https://") -> img
                                                img.startsWith("/") -> MAPS_BASE_URL.trimEnd('/') + img
                                                else -> img
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .pointerInput(index) {
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = { _: Offset -> /* cue */ },
                                                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                                            change.consume()
                                                            val dy = dragAmount.y
                                                            if (dy < -80f && index > 0) { viewModel.move(index, index - 1); scope.launch { delay(120) } }
                                                            else if (dy > 80f && index < waypoints.size - 1) { viewModel.move(index, index + 1); scope.launch { delay(120) } }
                                                        },
                                                        onDragEnd = {},
                                                        onDragCancel = {}
                                                    )
                                                }
                                                .background(androidx.compose.ui.graphics.Color(0xFF444444), shape = RoundedCornerShape(10.dp))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(56.dp).background(androidx.compose.ui.graphics.Color(0xFF2F2F2F), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                                if (!fullImg.isNullOrBlank()) {
                                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(fullImg).crossfade(true).build(), contentDescription = displayName, modifier = Modifier.fillMaxSize())
                                                } else {
                                                    Text(displayName.firstOrNull()?.toString() ?: "?", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${index + 1}. $displayName", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("${w.lat}, ${w.lng}", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.White)
                                            }

                                            IconButton(onClick = { viewModel.removeAt(index) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = androidx.compose.ui.graphics.Color(0xFFE57373))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column {
                        if (optimize != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Distancia total: ${metersToKmString(optimize.totalDistanceMeters.toInt())}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = androidx.compose.ui.graphics.Color.White)
                                Text(text = "Duración: ${secondsToReadable(optimize.totalDurationSeconds.toLong())}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = androidx.compose.ui.graphics.Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { toggleSheet(true) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Text("Ver estimación", color = androidx.compose.ui.graphics.Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Start route (activa followRoute automáticamente)
                        Button(onClick = {
                            if (uiState.waypoints.isEmpty()) { scope.launch { snackbarHostState.showSnackbar("No hay puntos en la ruta") }; return@Button }
                            val originLat = currentLocation?.latitude ?: 19.0420
                            val originLng = currentLocation?.longitude ?: -98.2063
                            routingActive = true
                            routePaused = false
                            toggleSheet(true)
                            followRoute = true // ahora se activa solo al iniciar
                            viewModel.startOptimize(originLat, originLng, mode = "driving")
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                            Text("Iniciar Ruta", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }
        }
    }

    // Mostrar errores
    LaunchedEffect(uiState.error) {
        if (uiState.error.isNotBlank()) snackbarHostState.showSnackbar(uiState.error)
    }

    // Cuando optimizeResponse y followRoute -> seguir ruta animando el mapView (no tocamos LocalContext dentro de coroutine)
    LaunchedEffect(optimizeResponse, routingActive, followRoute, currentLocation) {
        if (!routingActive || !followRoute) return@LaunchedEffect
        val optimize = optimizeResponse ?: return@LaunchedEffect
        if (optimize.polyline.isBlank()) return@LaunchedEffect

        // decode once
        val pts = decodePolylineToGeoPoints(optimize.polyline)
        if (pts.isEmpty()) return@LaunchedEffect

        // si tenemos activity la usamos para encontrar MapView y centrar inicialmente (calculado manualmente)
        val act = activity
        if (act != null) {
            try {
                var minLat = Double.POSITIVE_INFINITY
                var maxLat = Double.NEGATIVE_INFINITY
                var minLon = Double.POSITIVE_INFINITY
                var maxLon = Double.NEGATIVE_INFINITY
                for (p in pts) {
                    if (p.latitude < minLat) minLat = p.latitude
                    if (p.latitude > maxLat) maxLat = p.latitude
                    if (p.longitude < minLon) minLon = p.longitude
                    if (p.longitude > maxLon) maxLon = p.longitude
                }
                val latPad = (maxLat - minLat) * 0.02.coerceAtLeast(0.001)
                val lonPad = (maxLon - minLon) * 0.02.coerceAtLeast(0.001)
                val bbox = BoundingBox(maxLat + latPad, maxLon + lonPad, minLat - latPad, minLon - lonPad)
                val root = act.window?.decorView
                val mapView = findMapViewInViewGroup(root)
                mapView?.zoomToBoundingBox(bbox, true)
            } catch (_: Throwable) { /* ignore */ }
        }

        // loop que centra en el punto más cercano periódicamente mientras siga la ruta
        launch {
            val FOLLOW_INTERVAL_MS = 20_000L // 20 segundos
            while (routingActive && followRoute) {
                val act2 = activity
                val mapView = act2?.window?.decorView?.let { findMapViewInViewGroup(it) }
                val cur = currentLocation
                if (mapView != null && cur != null) {
                    var bestIdx = 0
                    var bestDist = Double.MAX_VALUE
                    pts.forEachIndexed { i, p ->
                        val d = distanceMeters(cur.latitude, cur.longitude, p.latitude, p.longitude)
                        if (d < bestDist) { bestDist = d; bestIdx = i }
                    }
                    val target = pts.getOrNull(bestIdx) ?: cur
                    try { mapView.controller.animateTo(target) } catch (_: Throwable) { /* ignore */ }
                }
                delay(FOLLOW_INTERVAL_MS)
            }
        }
    }
}

/** Helpers: buscar MapView en el árbol de vistas (recursiva) */
private fun findMapViewInViewGroup(view: View?): MapView? {
    if (view == null) return null
    if (view is MapView) return view
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            val found = findMapViewInViewGroup(child)
            if (found != null) return found
        }
    }
    return null
}



private suspend fun com.google.android.gms.tasks.Task<Location>.awaitSafely(): Location? = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { loc -> cont.resume(loc) }
    addOnFailureListener { ex -> cont.resumeWithException(ex) }
}
