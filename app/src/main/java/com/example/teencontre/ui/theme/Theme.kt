package com.example.teencontre.ui.theme

import android.app.Activity // 🛠️ NUEVO IMPORT
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect // 🛠️ NUEVO IMPORT
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb // 🛠️ NUEVO IMPORT
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView // 🛠️ NUEVO IMPORT
import androidx.core.view.WindowCompat // 🛠️ NUEVO IMPORT

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF9F9F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color.Black
)

@Composable
fun TeEncontreTheme(
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

    // 🛠️ CONTROL DE LAS BARRAS DE NAVEGACIÓN Y ESTADO DEL SISTEMA NATIVO
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Establece el color de fondo de la barra de estado superior y barra de navegación inferior
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb() // ← ESTO PINTA LA BARRA DE ABAJO OSCURA/CLARA

            // Hace que los iconos de las barras se adapten (oscuros en fondo claro, claros en fondo oscuro)
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
