package com.example.app_andando_ando.presentation.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun RadiusSelector(
    options: List<Int> = listOf(5000, 20000, 50000, 100000, 190910),
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(horizontal = 8.dp)) {
        options.forEach { meters ->
            val label = when (meters) {
                5000 -> "5 km"
                20000 -> "20 km"
                50000 -> "50 km"
                100000 -> "100 km"
                190910 -> "Estado"
                else -> "${meters / 1000} km"
            }
            Button(
                onClick = { onSelect(meters) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFECCACB),
                    contentColor = Color(0xFF452926)
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = label)
            }
        }
    }
}


