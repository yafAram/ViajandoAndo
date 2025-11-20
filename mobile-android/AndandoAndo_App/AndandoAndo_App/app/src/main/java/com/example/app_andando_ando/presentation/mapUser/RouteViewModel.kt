package com.example.app_andando_ando.presentation.mapUser


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_andando_ando.domain.model.mapUser.RouteOptimizeResponse
import com.example.app_andando_ando.data.repository.mapUser.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteUiState(
    val loading: Boolean = false,
    val result: RouteOptimizeResponse? = null,
    val error: String = ""
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val repo: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState

    /**
     * waypoints: lista de pares (lat, lng) en el orden actual.
     * mode: "driving" | "walking" | "bicycle"
     */
    fun optimize(originLat: Double, originLng: Double, waypoints: List<Pair<Double, Double>>, mode: String = "driving") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = "")
            try {
                val res = repo.optimize(originLat, originLng, waypoints, mode)
                _uiState.value = _uiState.value.copy(loading = false, result = res)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.localizedMessage ?: "Error optimizando ruta")
            }
        }
    }
}
