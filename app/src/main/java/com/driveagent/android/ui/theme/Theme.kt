package com.driveagent.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme colors
private val LightPrimary = Color(0xFF0066FF)
private val LightOnPrimary = Color.White
private val LightPrimaryContainer = Color(0xFFD6E3FF)
private val LightOnPrimaryContainer = Color(0xFF001B3D)

private val LightSecondary = Color(0xFF00CCCC)
private val LightOnSecondary = Color.White
private val LightSecondaryContainer = Color(0xFFB8F0F0)
private val LightOnSecondaryContainer = Color(0xFF003333)

private val LightError = Color(0xFFBA1A1A)
private val LightOnError = Color.White
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)

private val LightBackground = Color(0xFFFDFCFF)
private val LightOnBackground = Color(0xFF1A1C1E)
private val LightSurface = Color(0xFFFDFCFF)
private val LightOnSurface = Color(0xFF1A1C1E)
private val LightSurfaceVariant = Color(0xFFE1E2EC)
private val LightOnSurfaceVariant = Color(0xFF44464F)

// Dark theme colors
private val DarkPrimary = Color(0xFFAAC7FF)
private val DarkOnPrimary = Color(0xFF003062)
private val DarkPrimaryContainer = Color(0xFF004788)
private val DarkOnPrimaryContainer = Color(0xFFD6E3FF)

private val DarkSecondary = Color(0xFF66FFFF)
private val DarkOnSecondary = Color(0xFF005555)
private val DarkSecondaryContainer = Color(0xFF007777)
private val DarkOnSecondaryContainer = Color(0xFFB8F0F0)

private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

private val DarkBackground = Color(0xFF000000)
private val DarkOnBackground = Color(0xFFE3E2E6)
private val DarkSurface = Color(0xFF121212)
private val DarkOnSurface = Color(0xFFE3E2E6)
private val DarkSurfaceVariant = Color(0xFF44464F)
private val DarkOnSurfaceVariant = Color(0xFFC4C6D0)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun DriveAgentAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
