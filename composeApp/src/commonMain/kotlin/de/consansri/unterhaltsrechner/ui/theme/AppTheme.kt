package de.consansri.unterhaltsrechner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
internal fun AppTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
        CompositionLocalProvider(
            SignalColors.Local provides if (dark) SignalColors.Dark else SignalColors.Light,
            content = content,
        )
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF1D5C96),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondaryContainer = Color(0xFFE3E8F0),
    onSecondaryContainer = Color(0xFF1B2733),
    background = Color(0xFFF6F7F9),
    surface = Color(0xFFF6F7F9),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFEFF1F4),
    surfaceContainerHigh = Color(0xFFE6E9EE),
    outlineVariant = Color(0xFFD5D9DF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA2C9FF),
    onPrimary = Color(0xFF00315C),
    primaryContainer = Color(0xFF00477F),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondaryContainer = Color(0xFF2E3742),
    onSecondaryContainer = Color(0xFFDDE3EC),
    background = Color(0xFF111418),
    surface = Color(0xFF111418),
    surfaceContainerLow = Color(0xFF191C21),
    surfaceContainer = Color(0xFF1D2025),
    surfaceContainerHigh = Color(0xFF272A30),
)
