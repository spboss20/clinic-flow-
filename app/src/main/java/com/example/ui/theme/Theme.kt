package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ClinicTeal,
    onPrimary = Color.White,
    primaryContainer = ClinicTealContainer,
    onPrimaryContainer = ClinicTealDark,
    secondary = ClinicBlue,
    onSecondary = Color.White,
    secondaryContainer = ClinicBlueContainer,
    onSecondaryContainer = ClinicNavy,
    tertiary = ClinicNavyLight,
    onTertiary = Color.White,
    background = ClinicBackground,
    onBackground = ClinicNavyText,
    surface = ClinicSurface,
    onSurface = ClinicNavyText,
    surfaceVariant = ClinicSurfaceVariant,
    onSurfaceVariant = ClinicNavy,
    outline = ClinicBorder,
    outlineVariant = ClinicDivider,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DD0E1),
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D54),
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFF1565C0),
    onSecondaryContainer = Color(0xFFE3F2FD),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
)

@Composable
fun ClinicFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // For healthcare clarity and elderly readability, light theme is optimized for daylight and high contrast
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
