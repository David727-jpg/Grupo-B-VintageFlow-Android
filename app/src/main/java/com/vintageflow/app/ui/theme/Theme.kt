package com.vintageflow.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Negro,
    onPrimary = Blanco,
    primaryContainer = GrisClaro,
    onPrimaryContainer = Negro,
    secondary = GrisOscuro,
    onSecondary = Blanco,
    secondaryContainer = GrisFondo,
    onSecondaryContainer = Negro,
    tertiary = GrisOscuro,
    onTertiary = Blanco,
    background = Blanco,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = GrisFondo,
    onSurfaceVariant = GrisOscuro,
    outline = GrisMedio,
    outlineVariant = GrisClaro
)

private val DarkColorScheme = darkColorScheme(
    primary = TextoClaro,
    onPrimary = Negro,
    primaryContainer = GrisOscuroSuperficie,
    onPrimaryContainer = TextoClaro,
    secondary = GrisMedio,
    onSecondary = Negro,
    secondaryContainer = GrisOscuroSuperficie,
    onSecondaryContainer = TextoClaro,
    tertiary = GrisMedio,
    onTertiary = Negro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = FondoOscuro,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = GrisMedio,
    outline = GrisOscuro,
    outlineVariant = GrisOscuroSuperficie
)

@Composable
fun VintageFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // El color dinámico de Android 12+ se desactiva para que la app conserve
    // la identidad visual de los mockups en cualquier teléfono.
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