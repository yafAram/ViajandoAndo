package com.example.app_andando_ando.presentation.mapUser

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * MapScreen composable (osmdroid) — simple MapView integrado en Compose.
 *
 * Pegar este archivo en:
 * src/main/java/com/example/app_andando_ando/presentation/map/MapScreen.kt
 *
 * Dependencias necesarias:
 * implementation "org.osmdroid:osmdroid-android:6.1.18" (o versión que uses)
 *
 * Permisos (AndroidManifest.xml):
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *
 * Uso:
 * MapScreen { mapView -> /* guarda referencia y usa addMarker/addPolyline */ }
 */

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    initialLat: Double = 19.0420,
    initialLng: Double = -98.2063,
    initialZoom: Double = 12.0,
    onMapReady: ((MapView) -> Unit)? = null
) {
    val ctx = LocalContext.current

    // Ensure osmdroid configuration (user agent) is set — safe to call repeatedly.
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = ctx.packageName
    }

    // AndroidView hosts the MapView
    AndroidView(factory = { context ->
        // create MapView
        val mapView = MapView(context).apply {
            // tile source
            setTileSource(TileSourceFactory.MAPNIK)
            // enable pinch/zoom gestures
            setMultiTouchControls(true)
            // center & zoom
            controller.setZoom(initialZoom)
            controller.setCenter(GeoPoint(initialLat, initialLng))
            // layout params
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        // callback to caller
        onMapReady?.invoke(mapView)
        mapView
    }, modifier = modifier)
}

/**
 * Añade un marcador al MapView.
 * title es opcional (se muestra en el popup del marker).
 */
fun addMarker(context: Context, mapView: MapView, lat: Double, lng: Double, title: String? = null) {
    val marker = Marker(mapView)
    marker.position = GeoPoint(lat, lng)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    marker.title = title ?: ""
    mapView.overlays.add(marker)
    mapView.invalidate()
}

/**
 * Dibuja una polyline a partir de una lista de pares (lat, lng).
 * width: grosor del trazo en px.
 */
fun addPolyline(mapView: MapView, points: List<Pair<Double, Double>>, width: Float = 6f) {
    val line = Polyline()
    line.setPoints(points.map { GeoPoint(it.first, it.second) })
    line.outlinePaint.strokeWidth = width
    // puedes personalizar color así (opcional):
    // line.outlinePaint.color = android.graphics.Color.parseColor("#FF6200EE")
    mapView.overlays.add(line)
    mapView.invalidate()
}

/**
 * Limpia overlays (markers, polylines, etc).
 * Nota: si usas capas adicionales, re-implementar selectivamente.
 */
fun clearMap(mapView: MapView) {
    mapView.overlays.clear()
    mapView.invalidate()
}