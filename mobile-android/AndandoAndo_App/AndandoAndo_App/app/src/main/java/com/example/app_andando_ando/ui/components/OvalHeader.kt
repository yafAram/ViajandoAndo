package com.example.app_andando_ando.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.app_andando_ando.ui.theme.HeaderDark
import androidx.compose.ui.graphics.Color

@Composable
fun OvalHeader(
    title: String = "Cual es tu destino de hoy?",
    modifier: Modifier = Modifier,
    innerContent: @Composable (Modifier) -> Unit = {}
) {
    Box(modifier = modifier) {
        // Oval dark header: grande y con curva inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 120.dp, bottomEnd = 120.dp))
                .background(HeaderDark)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // placeholder area for search field
            innerContent(Modifier.fillMaxWidth())
        }
    }
}
