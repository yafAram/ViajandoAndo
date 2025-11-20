// src/main/java/com/example/app_andando_ando/utils/MapUtils.kt
package com.example.app_andando_ando.utils

import android.graphics.Paint
import android.graphics.Color
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

private const val TAG = "MapUtils"

internal fun parseSimplePolylinePairs(polyline: String): List<Pair<Double, Double>> {
    return polyline.split(";").mapNotNull { token ->
        val parts = token.trim().split(",")
        if (parts.size == 2) {
            val lat = parts[0].toDoubleOrNull()
            val lng = parts[1].toDoubleOrNull()
            if (lat != null && lng != null) lat to lng else null
        } else null
    }
}

private fun downsample(points: List<Pair<Double, Double>>, maxCount: Int): List<Pair<Double, Double>> {
    val pointsCount = points.size
    if (pointsCount <= maxCount) return points
    val step = pointsCount.toDouble() / maxCount.toDouble()
    val out = ArrayList<Pair<Double, Double>>(maxCount)
    var i = 0.0
    while (i < pointsCount) {
        val idx = i.toInt().coerceIn(0, pointsCount - 1)
        out.add(points[idx])
        i += step
    }
    if (out.isNotEmpty() && out.last() != points.last()) out.add(points.last())
    return out
}

/**
 * Forzar polyline AZUL en MapView (osmdroid). Limpia polylines previas.
 */
fun addPolylineFromString(mapView: MapView, polylineString: String?, maxPoints: Int = 2000) {
    if (polylineString.isNullOrBlank()) {
        Log.w(TAG, "addPolylineFromString: polylineString nulo/vacío")
        return
    }

    val rawPoints = try {
        parseSimplePolylinePairs(polylineString)
    } catch (t: Throwable) {
        Log.e(TAG, "parseSimplePolylinePairs fallo", t)
        emptyList()
    }

    if (rawPoints.isEmpty()) {
        Log.w(TAG, "addPolylineFromString: no hay puntos después del parseo")
        return
    }

    val points = if (rawPoints.size > maxPoints) downsample(rawPoints, maxPoints) else rawPoints
    val geoPoints = points.map { (lat, lng) -> GeoPoint(lat, lng) }

    mapView.post {
        try {
            // quitar polylines previas (solo Polyline)
            val toRemove = mapView.overlays.filterIsInstance<Polyline>().toList()
            toRemove.forEach { mapView.overlays.remove(it) }

            // crear polyline y asignar puntos
            val polyline = Polyline(mapView)
            polyline.setPoints(geoPoints)

            // color azul "bootstrap-like"
            val azulHex = Color.parseColor("#007BFF")

            // Forzar paint principal
            try {
                val p: Paint = polyline.paint
                p.apply {
                    color = azulHex
                    alpha = 255
                    strokeWidth = 8f
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }
            } catch (t: Throwable) {
                Log.w(TAG, "No se pudo ajustar polyline.paint: ${t.message}")
            }

            // Forzar outlinePaint (si existe)
            try {
                polyline.outlinePaint?.apply {
                    color = azulHex
                    alpha = 255
                    strokeWidth = 8f
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }
            } catch (_: Throwable) { /* ignore */ }

            // Invocar setColor por reflexión si existe (compatibilidad)
            runCatching { polyline.javaClass.getMethod("setColor", Int::class.javaPrimitiveType).invoke(polyline, azulHex) }

            polyline.title = "Ruta optimizada"

            // Añadir al final (último añadido = encima)
            mapView.overlays.add(polyline)
            mapView.invalidate()

            Log.d(TAG, "addPolylineFromString: añadida polyline AZUL con ${geoPoints.size} puntos")
        } catch (t: Throwable) {
            Log.e(TAG, "addPolylineFromString: error al añadir polyline", t)
        }
    }
}

fun clearPolylines(mapView: MapView) {
    mapView.post {
        try {
            val toRemove = mapView.overlays.filterIsInstance<Polyline>().toList()
            toRemove.forEach { mapView.overlays.remove(it) }
            if (toRemove.isNotEmpty()) Log.d(TAG, "clearPolylines: removidas ${toRemove.size} polylines")
            mapView.invalidate()
        } catch (t: Throwable) {
            Log.e(TAG, "clearPolylines: fallo", t)
        }
    }
}
