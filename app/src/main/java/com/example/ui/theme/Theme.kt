package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF031A24),
    primaryContainer = Color(0xFF0C3D52),
    onPrimaryContainer = Color(0xFFC7F3FF),
    secondary = WolfiusPurple,
    onSecondary = Color(0xFF131538),
    secondaryContainer = Color(0xFF262C5B),
    onSecondaryContainer = Color(0xFFE0E3FF),
    tertiary = JellyBeanBlue,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF334155)
)

private val LightColorScheme = darkColorScheme(
    // Default to dark palette to preserve OLED/LCD power and reduce rendering load on low-spec tablets
    primary = CyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = WolfiusAccent,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cyber holo aesthetic for low-spec tablets
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
