package com.example.app_andando_ando.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun WhiteActionButton(
    text: String,
    loading: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    elevation: Dp = 6.dp,
    onClick: () -> Unit
) {
    // convertimos la elevación a píxeles sin crear variables re-asignables
    val elevationPx = with(LocalDensity.current) { elevation.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            // apply a real shadow elevation at the graphics layer to avoid z-order issues
            .graphicsLayer {
                shadowElevation = elevationPx
                this.shape = shape
                clip = true
            }
            .background(color = Color.White, shape = shape)
            .clip(shape)
            .clickable(enabled = enabled) { if (enabled) onClick() }
            .zIndex(2f)
            .semantics { this.role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Procesando...", color = Color.Black, fontWeight = FontWeight.SemiBold)
            } else {
                Text(text = text, color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
