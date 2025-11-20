package com.example.app_andando_ando.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.app_andando_ando.utils.SecureStorage
import com.example.app_andando_ando.domain.model.mapUser.Waypoint
import com.example.app_andando_ando.domain.model.mapUser.RouteRequest
import com.example.app_andando_ando.domain.model.mapUser.Coordinate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage,
    private val gson: Gson
) {
    companion object {
        private const val PREFS_NAME = "va_session_prefs"
        private const val KEY_RADIUS_METERS = "KEY_RADIUS_METERS"
        private const val KEY_WAYPOINTS_JSON = "KEY_WAYPOINTS_JSON"
        const val DEFAULT_RADIUS_METERS = 20000 // 20 km
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ---------------- Token / Usuario (secure)
    fun saveToken(token: String) { secureStorage.saveToken(token) }
    fun getToken(): String? = secureStorage.getToken()
    fun clearToken() = secureStorage.clearToken()

    fun saveUserJson(json: String) = secureStorage.saveUser(json)
    fun getUserJson(): String? = secureStorage.getUserJson()
    fun clearUser() = secureStorage.clearUser()

    fun clearAll() = clearSession()

    fun clearSession() {
        clearToken()
        clearUser()
        prefs.edit().remove(KEY_RADIUS_METERS).apply()
        prefs.edit().remove(KEY_WAYPOINTS_JSON).apply()
    }

    // ---------------- Radius (SharedPreferences)
    fun saveRadiusMeters(radius: Int) {
        prefs.edit().putInt(KEY_RADIUS_METERS, radius).apply()
    }

    fun getRadiusMeters(): Int = prefs.getInt(KEY_RADIUS_METERS, DEFAULT_RADIUS_METERS)

    fun saveRadiusPreference(radius: Int) = saveRadiusMeters(radius)
    fun getRadiusPreference(): Int = getRadiusMeters()

    // ---------------- Waypoints (persistencia simple JSON)
    fun saveWaypointsList(list: List<Waypoint>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_WAYPOINTS_JSON, json).apply()
    }

    fun getWaypointsList(): List<Waypoint> {
        val json = prefs.getString(KEY_WAYPOINTS_JSON, null) ?: return emptyList()
        val type = object : TypeToken<List<Waypoint>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addWaypoint(waypoint: Waypoint) {
        val current = getWaypointsList().toMutableList()
        // evita duplicados por poiId
        if (current.none { it.poiId == waypoint.poiId }) {
            current.add(waypoint)
            saveWaypointsList(current)
        }
    }

    fun clearWaypoints() {
        prefs.edit().remove(KEY_WAYPOINTS_JSON).apply()
    }

    /**
     * Construye el objeto RouteRequest (JSON) usando el origen provisto.
     * Puedes usar gson.toJson(sessionManager.buildRouteRequest(...)) para enviar a la API.
     */
    fun buildRouteRequest(origin: Coordinate): RouteRequest {
        val waypoints = getWaypointsList()
        return RouteRequest(origin = origin, waypoints = waypoints)
    }
}





