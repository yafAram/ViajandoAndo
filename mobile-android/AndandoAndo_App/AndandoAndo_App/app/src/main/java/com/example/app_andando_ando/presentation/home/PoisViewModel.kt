package com.example.app_andando_ando.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.data.repository.mapUser.PoiRepository
import com.example.app_andando_ando.domain.model.mapUser.Poi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class PoisUiState(
    val loading: Boolean = false,
    val pois: List<Poi> = emptyList(),
    val error: String = ""
)

@HiltViewModel
class PoisViewModel @Inject constructor(
    private val repo: PoiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoisUiState())
    val uiState: StateFlow<PoisUiState> = _uiState

    // caché local de POIs tal y como los devuelve el repo en la última consulta (sin filtrar)
    private var cachedPois: List<Poi> = emptyList()

    private var loadJob: Job? = null

    /**
     * loadPois: trae desde repo y aplica filtros LOCALES (distance, category, name).
     * - lat/lng: referencia del usuario
     * - radius: metros (filtrado por Haversine <= radius)
     * - category: null = todos
     * - name: null = todos (substring, case-insensitive)
     *
     * Nota: repo.getPois(...) se usa para recuperar datos; como no queremos cambiar
     * la red, se puede pedir con el radius actual o con un radius mayor según tu preferencia.
     * Aquí pedimos con el radius que nos pases (evita spam con debounce).
     */
    fun loadPois(lat: Double, lng: Double, radius: Int = 2000, category: String? = null, name: String? = null) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = "")
            delay(250) // debounce
            try {
                // obtenemos desde repo (deja la llamada como estaba; solo la usaremos como "fuente")
                val list = repo.getPois(lat, lng, radius, category, name)
                cachedPois = list // guardamos crudos
                // filtramos localmente por distancia (Haversine), categoría y nombre
                val filtered = cachedPois.filter { poi ->
                    // distancia en metros entre user y poi
                    val d = distanceMeters(lat, lng, poi.latitude, poi.longitude)
                    if (d.isNaN()) return@filter false
                    if (d > radius) return@filter false
                    // category filter
                    if (!category.isNullOrBlank() && !category.equals("Todos", true)) {
                        val cat = poi.category ?: ""
                        if (!cat.equals(category, true)) return@filter false
                    }
                    // name filter
                    if (!name.isNullOrBlank()) {
                        val n = name.trim().lowercase()
                        val foundInName = poi.name?.lowercase()?.contains(n) ?: false
                        val foundInDesc = poi.description?.lowercase()?.contains(n) ?: false
                        if (!foundInName && !foundInDesc) return@filter false
                    }
                    true
                }
                _uiState.value = _uiState.value.copy(loading = false, pois = filtered)
            } catch (e: Exception) {
                // si la llamada falla, intentamos filtrar de la cache (si existe)
                if (cachedPois.isNotEmpty()) {
                    val fallback = cachedPois.filter { poi ->
                        val d = distanceMeters(lat, lng, poi.latitude, poi.longitude)
                        if (d.isNaN()) return@filter false
                        if (d > radius) return@filter false
                        if (!category.isNullOrBlank() && !category.equals("Todos", true)) {
                            val cat = poi.category ?: ""
                            if (!cat.equals(category, true)) return@filter false
                        }
                        if (!name.isNullOrBlank()) {
                            val n = name.trim().lowercase()
                            val foundInName = poi.name?.lowercase()?.contains(n) ?: false
                            val foundInDesc = poi.description?.lowercase()?.contains(n) ?: false
                            if (!foundInName && !foundInDesc) return@filter false
                        }
                        true
                    }
                    _uiState.value = _uiState.value.copy(loading = false, pois = fallback)
                } else {
                    _uiState.value = _uiState.value.copy(loading = false, error = e.localizedMessage ?: "Error cargando POIs")
                }
            }
        }
    }

    /** Haversine: devuelve distancia en metros entre (lat1,lng1) y (lat2,lng2) */
    private fun distanceMeters(lat1Degrees: Double, lon1Degrees: Double, lat2Degrees: Double, lon2Degrees: Double): Double {
        try {
            val lat1 = Math.toRadians(lat1Degrees)
            val lon1 = Math.toRadians(lon1Degrees)
            val lat2 = Math.toRadians(lat2Degrees)
            val lon2 = Math.toRadians(lon2Degrees)
            val dlat = lat2 - lat1
            val dlon = lon2 - lon1
            val a = sin(dlat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val R = 6371000.0
            return R * c
        } catch (t: Throwable) {
            return Double.NaN
        }
    }
}

