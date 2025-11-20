package com.example.app_andando_ando.presentation.poi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.data.repository.mapUser.PoiRepository
import com.example.app_andando_ando.domain.model.mapUser.Poi
import com.example.app_andando_ando.utils.RouteCache
import com.example.app_andando_ando.utils.WaypointSimple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PoiDetailViewModel @Inject constructor(
    private val poiRepo: PoiRepository,
    private val routeCache: RouteCache
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _poi = MutableStateFlow<Poi?>(null)
    val poi: StateFlow<Poi?> = _poi

    private val _isAdded = MutableStateFlow(false)
    val isAdded: StateFlow<Boolean> = _isAdded

    fun loadPoiById(id: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val p = poiRepo.getPoiById(id)
                _poi.value = p

                // comprobamos caché (RouteCache) si ya está agregado
                val added = routeCache.getAll().any { it.poiId == id }
                _isAdded.value = added

                Log.d("PoiDetailVM", "loadPoiById id=$id -> isAdded=$added")
            } catch (t: Throwable) {
                _error.value = t.localizedMessage ?: "Error cargando POI"
                Log.e("PoiDetailVM", "loadPoiById error", t)
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Usa la POI cargada en _poi para construir el WaypointSimple y guardarlo en RouteCache.
     */
    fun addPoiToRoute() {
        val current = _poi.value ?: return
        viewModelScope.launch {
            try {
                val wp = WaypointSimple(
                    poiId = current.poiId,
                    name = current.name,
                    lat = current.latitude,
                    lng = current.longitude
                )
                routeCache.add(wp)
                _isAdded.value = true
                Log.d("PoiDetailVM", "addPoiToRoute -> added ${current.poiId}")
            } catch (t: Throwable) {
                _error.value = t.localizedMessage ?: "Error agregando punto"
                Log.e("PoiDetailVM", "addPoiToRoute error", t)
            }
        }
    }
}

