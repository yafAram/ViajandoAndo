package com.example.app_andando_ando.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Si no quieres la base aquí, elimina la constante y asegúrate de pasar URLs absolutas desde el backend
private const val MAPS_BASE_URL = "https://viajandoandomapuserpoint.runasp.net"

@Composable
fun PoiListItem(
    name: String,
    subtitle: String = "",
    imageUrl: String? = null,
    visible: Boolean = true,
    cardElevationCollapsed: Dp = 4.dp,
    cardElevationExpanded: Dp = 10.dp,
    // onClick debe ser el último parámetro para permitir trailing lambda en llamadas
    onClick: () -> Unit
) {
    val inspection = LocalInspectionMode.current

    // Animaciones: escala + alpha + elevación pequeñas para dar sensación de entrada
    val targetScale = if (visible) 1f else 0.985f
    val targetAlpha = if (visible) 1f else 0f
    val targetElevation = if (visible) cardElevationExpanded else cardElevationCollapsed

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 260)
    )
    val elevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
    )

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .scale(scale)
            .alpha(alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Construir URL completa si viene relativa (empieza con '/')
            val fullImage = imageUrl?.let {
                when {
                    it.startsWith("http://") || it.startsWith("https://") -> it
                    it.startsWith("/") -> MAPS_BASE_URL.trimEnd('/') + it
                    else -> it // ya es relativa sin slash o ya absoluta: usar tal cual
                }
            }

            // Imagen izquierda (cuadro)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECECEC)),
                contentAlignment = Alignment.Center
            ) {
                if (!fullImage.isNullOrBlank()) {
                    AsyncImage(
                        model = fullImage,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder: inicial del nombre
                    Text(
                        text = name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
