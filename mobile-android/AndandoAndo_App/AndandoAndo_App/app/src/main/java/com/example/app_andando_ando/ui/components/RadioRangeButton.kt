package com.example.app_andando_ando.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_andando_ando.ui.theme.ButtonPink
import com.example.app_andando_ando.ui.theme.ButtonTextDark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun RadioRangeButton(
    options: List<Int>,
    selectedMeters: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Button(
        onClick = { expanded = true },
        colors = ButtonDefaults.buttonColors(containerColor = ButtonPink),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.padding(start = 8.dp, top = 6.dp)
    ) {
        Text(text = "Radio: ${selectedMeters / 1000} km", color = ButtonTextDark)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { opt ->
            DropdownMenuItem(
                text = {
                    Text(text = "${opt / 1000} km", color = ButtonTextDark)
                },
                onClick = {
                    onSelected(opt)
                    expanded = false
                }
            )
        }
    }
}
