package com.example.app_andando_ando.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_andando_ando.ui.theme.ButtonPinkBg
import com.example.app_andando_ando.ui.theme.ButtonTextBrown
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfileHeader(
    name: String,
    email: String,
    avatarUrl: String? = null,
    onEditProfile: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp)
    ) {
        // Avatar circle
        val imageModifier = Modifier
            .size(100.dp)
            .clip(CircleShape)

        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        } else {
            // Placeholder circle
            Box(
                modifier = imageModifier
                    .background(ButtonPinkBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Small placeholder letter
                Text(
                    text = name.takeIf { it.isNotBlank() }?.firstOrNull()?.toString() ?: "U",
                    color = ButtonTextBrown,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = email,
            fontSize = 12.sp,
            color = androidx.compose.ui.graphics.Color(0xFFBFBFBF),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Edit profile button
        Button(
            onClick = onEditProfile,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonPinkBg)
        ) {
            Text(text = "Editar perfil", color = ButtonTextBrown)
        }
    }
}
