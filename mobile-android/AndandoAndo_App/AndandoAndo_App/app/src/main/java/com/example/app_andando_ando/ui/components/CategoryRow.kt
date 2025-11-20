package com.example.app_andando_ando.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.app_andando_ando.ui.theme.ButtonPink
import com.example.app_andando_ando.ui.theme.ButtonTextDark
import androidx.compose.ui.unit.sp

@Composable
fun CategoryRow(
    categories: List<String>,
    selected: String? = null,
    onSelected: (String?) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scroll)
            .padding(start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { cat ->
            CategoryChip(
                label = cat,
                isSelected = selected == cat,
                onClick = { onSelected(if (selected == cat) null else cat) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ButtonPink,
        shadowElevation = if (isSelected) 6.dp else 4.dp,
        modifier = Modifier
            .padding(vertical = 6.dp)
    ) {
        androidx.compose.material3.TextButton(
            onClick = onClick,
            content = {
                Text(
                    text = label,
                    color = ButtonTextDark,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        )
    }
}

