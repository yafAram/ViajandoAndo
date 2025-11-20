package com.example.app_andando_ando.presentation.route

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.osmdroid.util.GeoPoint

suspend fun getLastKnownLocationAsGeoPoint(
    context: Context,
    fusedClient: FusedLocationProviderClient
): GeoPoint? = suspendCancellableCoroutine { continuation ->

    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasFineLocation && !hasCoarseLocation) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    try {
        fusedClient.lastLocation.addOnCompleteListener { task ->
            try {
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    continuation.resume(GeoPoint(location.latitude, location.longitude))
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    } catch (securityException: SecurityException) {
        continuation.resume(null)
    } catch (e: Exception) {
        continuation.resume(null)
    }
}