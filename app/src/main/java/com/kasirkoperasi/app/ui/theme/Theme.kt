package com.kasirkoperasi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FreshMint,
    onPrimary = DarkGreen,
    primaryContainer = LeafGreen,
    onPrimaryContainer = Color.White,
    secondary = WarmAccent,
    onSecondary = DarkGreen,
    background = DarkGreen,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = LeafGreen,
    onSurfaceVariant = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Color(0xFF410002),
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = Color.White,
    primaryContainer = FreshMint,
    onPrimaryContainer = DeepGreen,
    secondary = LeafGreen,
    onSecondary = Color.White,
    secondaryContainer = FreshMint,
    onSecondaryContainer = DeepGreen,
    tertiary = WarmAccent,
    background = CreamBackground,
    onBackground = Color(0xFF17221B),
    surface = Color.White,
    onSurface = Color(0xFF17221B),
    surfaceVariant = SoftGray,
    onSurfaceVariant = MutedText,
    outline = LineSoft,
    errorContainer = DangerSoft,
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun KasirKoperasiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
