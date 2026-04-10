package com.ghuba.taprux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF0D47A1),
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF0D47A1),
        onPrimaryContainer = Color(0xFFBBDEFB),
        secondary = Color(0xFFBDBDBD),
        onSecondary = Color(0xFF424242),
        secondaryContainer = Color(0xFF424242),
        onSecondaryContainer = Color(0xFFE0E0E0),
        tertiary = Color(0xFF90CAF9),
        onTertiary = Color(0xFF0D47A1),
        error = Color(0xFFEF5350),
        onError = Color(0xFF1C1B1F),
        background = Color(0xFFCCC2DC),
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        surfaceVariant = Color(0xFF1C1C1C),
        onSurfaceVariant = Color(0xFFBDBDBD),
        outline = Color(0xFF757575),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF1976D2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF757575),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0E0E0),
        onSecondaryContainer = Color(0xFF424242),
        tertiary = Color(0xFF1976D2),
        onTertiary = Color.White,
        error = Color(0xFFD32F2F),
        onError = Color.White,
        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFF757575),
        outline = Color(0xFFBDBDBD),
    )

@Composable
fun TapruxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
  val colorScheme =
      when {
        dynamicColor -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }

  MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
