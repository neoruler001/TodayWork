package com.todaywork.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFB8F0C0),
    secondary        = Secondary,
    onSecondary      = Color.White,
    tertiary         = Tertiary,
    background       = BgLight,
    surface          = SurfaceLight,
    onBackground     = OnBgLight,
    onSurface        = OnBgLight,
    error            = Error
)

private val DarkColorScheme = darkColorScheme(
    primary          = PrimaryVar,
    onPrimary        = Color.Black,
    primaryContainer = Color(0xFF1B3D1F),
    secondary        = SecondaryVar,
    onSecondary      = Color.Black,
    tertiary         = Tertiary,
    background       = BgDark,
    surface          = SurfaceDark,
    onBackground     = OnBgDark,
    onSurface        = OnBgDark,
    error            = Error
)

@Composable
fun TodayWorkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
