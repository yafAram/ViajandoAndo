package com.example.app_andando_ando.utils

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import org.osmdroid.util.GeoPoint
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.*

// -----------------------------------------------------------
//  UBICACIÓN ACTUAL COMO GeoPoint
// -----------------------------------------------------------
suspend fun getLastKnownLocationAsGeoPoint(
    context: Context,
    fused: FusedLocationProviderClient
): GeoPoint? = suspendCancellableCoroutine { cont ->
    fused.lastLocation
        .addOnSuccessListener { loc: Location? ->
            if (loc != null) cont.resume(GeoPoint(loc.latitude, loc.longitude))
            else cont.resume(null)
        }
        .addOnFailureListener { e -> cont.resumeWithException(e) }
}

// -----------------------------------------------------------
//  DECODIFICAR POLYLINE (Google encoded polyline → GeoPoint)
// -----------------------------------------------------------
fun decodePolylineToGeoPoints(encoded: String): List<GeoPoint> {
    val poly = ArrayList<GeoPoint>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1F) shl shift)
            shift += 5
        } while (b >= 0x20)

        val dLat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dLat

        shift = 0
        result = 0

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1F) shl shift)
            shift += 5
        } while (b >= 0x20)

        val dLng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dLng

        poly.add(GeoPoint(lat / 1E5, lng / 1E5))
    }

    return poly
}

// -----------------------------------------------------------
//  DISTANCIA EN METROS ENTRE DOS COORDENADAS
// -----------------------------------------------------------
fun distanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val R = 6371000.0 // metros
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

// -----------------------------------------------------------
//  FORMATO: metros → "X km"
// -----------------------------------------------------------
fun metersToKmString(meters: Int): String {
    return if (meters < 1000) {
        "$meters m"
    } else {
        String.format("%.2f km", meters / 1000f)
    }
}

// -----------------------------------------------------------
//  FORMATO: segundos → "h m s"
// -----------------------------------------------------------
fun secondsToReadable(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60

    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0) append("${m}m ")
        if (s > 0 || (h == 0L && m == 0L)) append("${s}s")
    }.trim()
}

// -----------------------------------------------------------
//  EXTENSIONES DE GeoPoint
// -----------------------------------------------------------
val GeoPoint.latitude: Double get() = this.latitude
val GeoPoint.longitude: Double get() = this.longitude
