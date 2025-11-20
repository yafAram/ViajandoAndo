package com.example.app_andando_ando.presentation.poi

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_andando_ando.domain.model.mapUser.Poi
import java.text.NumberFormat
import java.util.Locale

private const val MAPS_BASE_URL = "https://viajandoandomapuserpoint.runasp.net"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoiDetailScreen(
    poiId: String,
    navController: NavController? = null,
    viewModel: PoiDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    LaunchedEffect(poiId) { viewModel.loadPoiById(poiId) }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val poi by viewModel.poi.collectAsState()
    val isAdded by viewModel.isAdded.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController?.popBackStack()
                                ?: activity?.onBackPressedDispatcher?.onBackPressed()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF424242)
                ),
                modifier = Modifier.height(48.dp) // header más delgado
            )
        },

        snackbarHost = { SnackbarHost(snackbarHostState) },

        bottomBar = {
            Surface(
                tonalElevation = 6.dp
            ) {
                Button(
                    onClick = { viewModel.addPoiToRoute() },
                    enabled = !isAdded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isAdded) "Añadido a mi ruta ✓" else "Añadir a mi ruta", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // IMAGEN FIJA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // altura estática
                    .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val raw = poi?.imageUrl
                val img = raw?.let {
                    if (it.startsWith("http://") || it.startsWith("https://")) it
                    else MAPS_BASE_URL.trimEnd('/') + it
                }

                if (!img.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(img).crossfade(true).build(),
                        contentDescription = poi?.name ?: "Imagen POI",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = poi?.name ?: "Lugar", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null && error!!.isNotEmpty() -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                poi != null -> {
                    // INFO PRINCIPAL
                    Text(
                        text = poi!!.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = poi!!.category ?: "Categoría no disponible",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = poi!!.description ?: "Sin descripción.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // MENÚ
                    if (!poi!!.menu.isNullOrEmpty()) {
                        Text(
                            "Menú",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        val nf = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
                        poi!!.menu.forEach { item ->
                            MenuItemRow(
                                name = item.name,
                                description = item.description,
                                price = item.price
                                , formatter = nf)
                        }

                        Spacer(Modifier.height(20.dp))
                    }

                    // HORARIOS
                    Text(
                        "Horario",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    poi!!.openingHours?.forEach {
                        Text("${dayOfWeekName(it.dayOfWeek)}   ${it.open} - ${it.close}", modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    Spacer(modifier = Modifier.height(120.dp)) // espacio final para no quedar tapado por el botón
                }
            }
        }
    }

    LaunchedEffect(isAdded) {
        if (isAdded) snackbarHostState.showSnackbar("Punto agregado a tu ruta ✓")
    }
}

/**
 * Ahora acepta nullable para name/description y nullable Double para price.
 * Recibe también un formatter opcional para formatear moneda.
 */
@Composable
fun MenuItemRow(name: String?, description: String?, price: Double?, formatter: NumberFormat? = null) {
    val displayName = name ?: ""
    val displayDesc = description ?: ""
    val displayPrice = (price ?: 0.0)
    val formattedPrice = formatter?.format(displayPrice) ?: String.format("$%.2f", displayPrice)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = displayName, fontWeight = FontWeight.SemiBold, maxLines = 1)
            if (displayDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = displayDesc, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = formattedPrice, fontWeight = FontWeight.Bold)
    }
}

private fun dayOfWeekName(day: Int): String = when (day) {
    1 -> "Lunes"
    2 -> "Martes"
    3 -> "Miércoles"
    4 -> "Jueves"
    5 -> "Viernes"
    6 -> "Sábado"
    7 -> "Domingo"
    else -> "Día $day"
}
