package com.example.app_andando_ando.presentation.route

import org.osmdroid.util.GeoPoint
import kotlin.math.absoluteValue

/** Decodificador robusto: soporta "lat,lng;..." y encoded polyline.
 *  Intenta varias correcciones: swap lat/lng, escala (1e5 / 1e6).
 *  Devuelve lista válida o vacía si no pudo obtener coordenadas razonables.
 */
fun decodePolylineToGeoPoints(encoded: String): List<GeoPoint> {
    if (encoded.isBlank()) return emptyList()

    // 1) si formato "lat,lng;lat,lng;..."
    if (encoded.contains(";") && encoded.contains(",")) {
        val raw = encoded.split(";").mapNotNull { token ->
            val parts = token.trim().split(",")
            if (parts.size >= 2) {
                val a = parts[0].toDoubleOrNull()
                val b = parts[1].toDoubleOrNull()
                if (a != null && b != null) GeoPoint(a, b) else null
            } else null
        }.toMutableList()
        return sanitizeAndFixPoints(raw)
    }

    // 2) intentar decodificar encoded polyline (Google encoded)
    fun decodeWithScale(scale: Double): List<GeoPoint> {
        val pts = mutableListOf<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        try {
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].code - 63
                    result = result or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20 && index < len)
                val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].code - 63
                    result = result or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20 && index < len)
                val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
                lng += dlng

                val latD = lat.toDouble() / scale
                val lngD = lng.toDouble() / scale
                pts.add(GeoPoint(latD, lngD))
            }
        } catch (t: Throwable) {
            // decode falló; retornamos lo que tengamos (posible vacio)
        }
        return pts
    }

    // probar con escala usual 1e5
    var pts = decodeWithScale(1E5)
    // si puntos inválidos (lat fuera rango), probar 1e6
    if (!pointsLookValid(pts)) {
        val pts2 = decodeWithScale(1E6)
        if (pointsLookValid(pts2)) pts = pts2
    }

    // sanitizar + posible swap
    return sanitizeAndFixPoints(pts.toMutableList())
}

/** Comprueba heurísticamente si los puntos parecen validos (lat en -90..90, lon -180..180).
 *  Requiere que al menos la mayoría estén en rango.
 */
private fun pointsLookValid(pts: List<GeoPoint>): Boolean {
    if (pts.isEmpty()) return false
    val inRange = pts.count { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    return inRange >= (pts.size * 0.6) // 60% ok -> consider valid
}

/** Intenta arreglar swaps lat/lng si mejora la validez; además elimina puntos claramente inválidos. */
private fun sanitizeAndFixPoints(raw: MutableList<GeoPoint>): List<GeoPoint> {
    if (raw.isEmpty()) return emptyList()

    val validNow = raw.count { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    if (validNow >= raw.size * 0.6) {
        // ya es mayormente válido: filtrar outliers y devolver
        return raw.filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    }

    // intentar swap lat<->lon
    val swapped = raw.map { GeoPoint(it.longitude, it.latitude) }
    val validSwapped = swapped.count { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    if (validSwapped >= raw.size * 0.6) {
        return swapped.filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    }

    // si no funcionó, intentar filtrar valores extremos y devolver lo que quede
    val filtered = raw.filter { it.latitude.absoluteValue <= 1000 && it.longitude.absoluteValue <= 1000 } // poda grossa
        .filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    return filtered
}
