package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeminiColorScheme = darkColorScheme(
    primary = GeminiSparkBlue,
    onPrimary = Color(0xFF002A66),
    primaryContainer = GeminiSurfaceContainer,
    onPrimaryContainer = GeminiTextPrimary,
    secondary = GeminiSparkPurple,
    onSecondary = Color(0xFF2D006A),
    tertiary = GeminiSparkCyan,
    background = GeminiBackground,
    onBackground = GeminiTextPrimary,
    surface = GeminiSurface,
    onSurface = GeminiTextPrimary,
    surfaceVariant = GeminiSurfaceContainer,
    onSurfaceVariant = GeminiTextSecondary,
    outline = GeminiBorder,
    outlineVariant = GeminiBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // OpenCodeMovil strictly enforces the elegant Gemini Dark Theme
    MaterialTheme(
        colorScheme = GeminiColorScheme,
        typography = Typography,
        content = content
    )
}
