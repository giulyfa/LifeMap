package com.example.lifemap.ui.theme

import android.app.Activity
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

// PALETTE SCURA
private val DarkColorScheme = darkColorScheme(
    primary = Green2,
    secondary = Gold,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF444444)
)

// PALETTE CHIARA
private val LightColorScheme = lightColorScheme(
    primary = Green2,
    secondary = Gold,
    background = Color(0xFFFBF9F4),
    surface = Color(0xFFFFFEF9),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFEEEEEE)
)

@Composable
fun LifeMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
