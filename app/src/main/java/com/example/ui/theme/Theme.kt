package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    secondary = CrimsonSecondary,
    tertiary = CyberOcean,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    secondary = CrimsonSecondary,
    tertiary = CyberOcean,
    background = DarkBackground,       // Keep immersive dark theme experience
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark mode for premium short-video feel
  dynamicColor: Boolean = false, // Force consistent custom color scheme branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
