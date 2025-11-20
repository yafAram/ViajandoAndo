package com.example.app_andando_ando.presentation.route

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.data.repository.mapUser.RouteRepository
import com.example.app_andando_ando.domain.model.mapUser.RouteOptimizeResponse
import com.example.app_andando_ando.utils.RouteCache
import com.example.app_andando_ando.utils.WaypointSimple
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG_V = "RouteViewModel"

data class RouteUiState(
    val waypoints: List<WaypointSimple> = emptyList(),
    val loading: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val routeCache: RouteCache,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState(waypoints = emptyList()))
    val uiState: StateFlow<RouteUiState> = _uiState

    private val _optimizeResponse = MutableStateFlow<RouteOptimizeResponse?>(null)
    val optimizeResponse: StateFlow<RouteOptimizeResponse?> = _optimizeResponse

    init {
        refreshFromCache()
        Log.d(TAG_V, "ViewModel inicializado - Cache refrescado")
    }

    fun refreshFromCache() {
        val cachedWaypoints = routeCache.getAll()
        _uiState.value = _uiState.value.copy(
            waypoints = cachedWaypoints,
            loading = false,
            error = ""
        )
        Log.d(TAG_V, "Cache actualizado -> ${cachedWaypoints.size} waypoints")

        // Debug: mostrar IDs de waypoints
        cachedWaypoints.forEachIndexed { index, waypoint ->
            Log.d(TAG_V, "Waypoint $index: ${waypoint.poiId} - ${waypoint.name}")
        }
    }

    fun addWaypoint(w: WaypointSimple) {
        routeCache.add(w)
        // ACTUALIZACIÓN INMEDIATA
        _uiState.value = _uiState.value.copy(waypoints = routeCache.getAll())
        Log.d(TAG_V, "Waypoint añadido: ${w.poiId} - Total: ${_uiState.value.waypoints.size}")
    }

    fun removeAt(index: Int) {
        routeCache.removeAt(index)
        // ACTUALIZACIÓN INMEDIATA
        _uiState.value = _uiState.value.copy(waypoints = routeCache.getAll())
        Log.d(TAG_V, "Waypoint eliminado en índice $index - Total: ${_uiState.value.waypoints.size}")
    }

    fun moveUp(index: Int) {
        if (index <= 0) return
        routeCache.move(index, index - 1)
        refreshFromCache()
    }

    fun moveDown(index: Int) {
        val list = _uiState.value.waypoints
        if (index < 0 || index >= list.size - 1) return
        routeCache.move(index, index + 1)
        refreshFromCache()
    }

    fun clearRoute() {
        routeCache.clear()
        refreshFromCache()
    }

    fun move(from: Int, to: Int) {
        try {
            if (from == to) return
            val list = _uiState.value.waypoints
            if (from < 0 || from >= list.size || to < 0 || to >= list.size) return
            routeCache.move(from, to)
            // ACTUALIZACIÓN INMEDIATA
            _uiState.value = _uiState.value.copy(waypoints = routeCache.getAll())
        } catch (t: Throwable) {
            Log.e(TAG_V, "move error", t)
        }
    }

    fun getCacheJson(): String? {
        return routeCache.dumpJson()
    }

    fun startOptimize(originLat: Double, originLng: Double, mode: String = "driving") {
        val wps = _uiState.value.waypoints
        if (wps.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "No hay puntos en la ruta.")
            Log.w(TAG_V, "startOptimize abortado: no hay waypoints")
            return
        }

        try {
            val previewWaypoints = wps.map { wp ->
                mapOf("poiId" to wp.poiId, "coordinate" to mapOf("lat" to wp.lat, "lng" to wp.lng))
            }
            val previewReq = mapOf("origin" to mapOf("lat" to originLat, "lng" to originLng), "waypoints" to previewWaypoints, "mode" to mode)
            Log.d(TAG_V, "startOptimize -> origin=($originLat,$originLng) mode=$mode waypointsCount=${wps.size}")
            Log.d(TAG_V, "startOptimize -> preview request JSON: ${gson.toJson(previewReq)}")
        } catch (t: Throwable) {
            Log.e(TAG_V, "startOptimize -> error serializando preview", t)
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = "")
            _optimizeResponse.value = null
            try {
                val coords = wps.map { Pair(it.lat, it.lng) }
                val res = routeRepository.optimize(originLat, originLng, coords, mode)
                _optimizeResponse.value = res
                Log.d(TAG_V, "startOptimize -> optimize finished: distance=${res.totalDistanceMeters} duration=${res.totalDurationSeconds}")
            } catch (t: Throwable) {
                val msg = t.message ?: "Error al optimizar"
                _uiState.value = _uiState.value.copy(error = msg)
                Log.e(TAG_V, "startOptimize -> error", t)
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    fun getCachedWaypoints(): List<WaypointSimple> {
        return routeCache.getAll()
    }
}
