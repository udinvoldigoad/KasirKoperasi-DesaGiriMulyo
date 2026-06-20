package com.kasirkoperasi.app.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.settings.StoreProfileStore
import com.kasirkoperasi.app.ui.theme.DeepGreen

@Composable
fun KoperasiLogo(
    modifier: Modifier = Modifier,
    logoUri: String? = null,
    fallbackText: String = "K",
    size: Dp = 40.dp,
) {
    val context = LocalContext.current
    val logoBitmap = remember(logoUri) {
        StoreProfileStore.loadLogoBitmap(context, logoUri)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(DeepGreen),
        contentAlignment = Alignment.Center,
    ) {
        if (logoBitmap != null) {
            Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = "Logo koperasi",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = fallbackText.take(2).ifBlank { "K" },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
